package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.dto.JianpuParseDTO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.entity.MusicNote;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.mapper.MusicNoteMapper;
import com.kingdomstudio.modules.music.mapper.MusicTaskMapper;
import com.kingdomstudio.modules.music.parser.JianpuParser;
import com.kingdomstudio.modules.music.parser.MidiParser;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 音乐任务服务测试：落库、级联删除、映射入口。
 *
 * <p>Mapper 用替身，解析器与映射引擎用真实现 —— 要验的是「服务把东西串对了没有」，
 * 把解析也替掉就只剩空壳断言了。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MusicTaskServiceTest {

	@Mock
	private MusicTaskMapper musicTaskMapper;
	@Mock
	private MusicNoteMapper musicNoteMapper;
	@Mock
	private InstrumentProfileMapper instrumentProfileMapper;

	private MusicTaskService service;

	/** 模拟数据库自增主键：插入后实体拿到 id，后续 detail() 才查得到 */
	private final AtomicLong idSequence = new AtomicLong(1);

	@BeforeEach
	void setUp() {
		service = new MusicTaskService(musicTaskMapper, musicNoteMapper, instrumentProfileMapper,
				new MidiParser(), new JianpuParser(), new InstrumentMappingService());
		idSequence.set(1);
		when(musicTaskMapper.insert(any(MusicTask.class))).thenAnswer(invocation -> {
			MusicTask task = invocation.getArgument(0);
			task.setId(idSequence.getAndIncrement());
			return 1;
		});
	}

	@Test
	@DisplayName("简谱解析：任务与音符一起落库，返回详情里带音名与音域")
	void shouldPersistJianpuTask() {
		JianpuParseDTO request = new JianpuParseDTO();
		request.setName("小星星");
		request.setJianpu("1=C 4/4 BPM=96\n1 1 5 5 | 6 6 5 -");

		MusicTask stored = new MusicTask();
		stored.setId(1L);
		stored.setName("小星星");
		stored.setSourceType("JIANPU");
		stored.setNoteCount(8);
		stored.setTempoBpm(96);
		stored.setTimeSignature("4/4");
		stored.setDurationMs(5000);
		stored.setPitchLow(60);
		stored.setPitchHigh(69);
		stored.setStatus("READY");
		when(musicTaskMapper.selectById(1L)).thenReturn(stored);
		when(musicNoteMapper.selectList(any())).thenReturn(List.of());

		MusicTaskDetailVO detail = service.createFromJianpu(request);

		ArgumentCaptor<MusicTask> taskCaptor = ArgumentCaptor.forClass(MusicTask.class);
		verify(musicTaskMapper).insert(taskCaptor.capture());
		assertEquals("小星星", taskCaptor.getValue().getName());
		assertEquals("JIANPU", taskCaptor.getValue().getSourceType());
		assertEquals(96, taskCaptor.getValue().getTempoBpm());
		assertEquals(7, taskCaptor.getValue().getNoteCount(), "1 1 5 5 6 6 5 - 是 7 个音：末尾的减号只是把最后一个音延长一拍");
		assertEquals(60, taskCaptor.getValue().getPitchLow());
		assertEquals("C4–A4", detail.getPitchRange());
		verify(musicNoteMapper, times(7)).insert(any(MusicNote.class));
	}

	@Test
	@DisplayName("简谱里读不到音符：直接报错，不留下半条任务")
	void shouldRejectEmptyJianpu() {
		JianpuParseDTO request = new JianpuParseDTO();
		request.setJianpu("1=C 4/4\n（这里没有音符）");

		assertThrows(BusinessException.class, () -> service.createFromJianpu(request));
		verify(musicTaskMapper, never()).insert(any(MusicTask.class));
	}

	@Test
	@DisplayName("MIDI 上传：按事件数落库音符")
	void shouldPersistMidiTask() throws Exception {
		Sequence sequence = new Sequence(Sequence.PPQ, 480);
		Track track = sequence.createTrack();
		for (int index = 0; index < 4; index++) {
			track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60 + index * 2, 90), index * 480L));
			track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60 + index * 2, 0), (index + 1) * 480L));
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		MidiSystem.write(sequence, 1, buffer);
		MockMultipartFile file = new MockMultipartFile("file", "小星星.mid", "audio/midi", buffer.toByteArray());

		MusicTask stored = new MusicTask();
		stored.setId(1L);
		stored.setName("小星星");
		stored.setSourceType("MIDI");
		stored.setNoteCount(4);
		stored.setTempoBpm(120);
		stored.setTimeSignature("4/4");
		stored.setDurationMs(2000);
		stored.setPitchLow(60);
		stored.setPitchHigh(66);
		when(musicTaskMapper.selectById(1L)).thenReturn(stored);
		when(musicNoteMapper.selectList(any())).thenReturn(List.of());

		MusicTaskDetailVO detail = service.createFromMidi(file);

		assertEquals("MIDI", detail.getSourceType());
		verify(musicNoteMapper, times(4)).insert(any(MusicNote.class));
		ArgumentCaptor<MusicNote> captor = ArgumentCaptor.forClass(MusicNote.class);
		verify(musicNoteMapper, times(4)).insert(captor.capture());
		assertEquals(1, captor.getAllValues().get(0).getSeqNo(), "序号从 1 开始");
		assertEquals("C4", captor.getAllValues().get(0).getNoteName());
	}

	@Test
	@DisplayName("上传的不是 MIDI：给一句能看懂的中文错误")
	void shouldRejectBrokenMidi() {
		MockMultipartFile file = new MockMultipartFile("file", "坏文件.mid", "audio/midi", "这不是 MIDI".getBytes());

		BusinessException error = assertThrows(BusinessException.class, () -> service.createFromMidi(file));
		assertTrue(error.getMessage().contains("解析不了"), error.getMessage());
		verify(musicTaskMapper, never()).insert(any(MusicTask.class));
	}

	@Test
	@DisplayName("任务不存在：404 而不是返回空对象")
	void shouldReportMissingTask() {
		when(musicTaskMapper.selectById(99L)).thenReturn(null);

		assertThrows(BusinessException.class, () -> service.detail(99L));
		assertThrows(BusinessException.class, () -> service.delete(99L));
	}

	@Test
	@DisplayName("删除任务：先删音符再删任务，不留孤儿音符")
	void shouldCascadeDeleteNotes() {
		MusicTask task = new MusicTask();
		task.setId(5L);
		when(musicTaskMapper.selectById(5L)).thenReturn(task);

		service.delete(5L);

		verify(musicNoteMapper).delete(any());
		verify(musicTaskMapper).deleteById(5L);
	}

	@Test
	@DisplayName("按键映射：任务里的音符交给映射引擎，并按档案设置落键")
	void shouldMapStoredTask() {
		MusicTask task = new MusicTask();
		task.setId(7L);
		task.setName("小星星");
		task.setSourceType("JIANPU");
		task.setSourceRef("1 1 5 5");
		task.setTempoBpm(96);
		task.setTimeSignature("4/4");
		task.setDurationMs(2500);
		when(musicTaskMapper.selectById(7L)).thenReturn(task);

		List<MusicNote> rows = new ArrayList<>();
		int[] pitches = {60, 60, 67, 67};
		int seq = 1;
		for (int pitch : pitches) {
			MusicNote row = new MusicNote();
			row.setTaskId(7L);
			row.setSeqNo(seq);
			row.setPitch(pitch);
			row.setStartMs((seq - 1) * 500);
			row.setDurationMs(500);
			row.setVelocity(90);
			row.setTrackNo(0);
			rows.add(row);
			seq++;
		}
		when(musicNoteMapper.selectList(any())).thenReturn(rows);

		InstrumentProfile profile = new InstrumentProfile();
		profile.setId(3L);
		profile.setName("光遇式 15 键");
		profile.setMappingMode("DIATONIC");
		profile.setScale("MAJOR");
		profile.setKeyLayout("[\"Z\",\"X\",\"C\",\"V\",\"B\",\"N\",\"M\"]");
		profile.setBasePitch(60);
		profile.setTranspose(0);
		profile.setOctaveShift(0);
		profile.setUnmappedStrategy("SKIP");
		when(instrumentProfileMapper.selectById(3L)).thenReturn(profile);

		MappingRequestDTO request = new MappingRequestDTO();
		request.setProfileId(3L);
		KeySequenceVO sequence = service.mapKeys(7L, request);

		assertEquals(7L, sequence.getTaskId());
		assertEquals("小星星", sequence.getTaskName());
		assertEquals(List.of("Z", "Z", "B", "B"), sequence.getStrokes().stream()
				.map(stroke -> stroke.getKeys().get(0)).toList());
		assertEquals(4, sequence.getMappedCount());
		assertTrue(sequence.getExportText().contains("Z=C4"));
	}

	@Test
	@DisplayName("按键映射：档案不存在时明确报错")
	void shouldReportMissingProfile() {
		MusicTask task = new MusicTask();
		task.setId(7L);
		when(musicTaskMapper.selectById(7L)).thenReturn(task);
		when(instrumentProfileMapper.selectById(4L)).thenReturn(null);

		MappingRequestDTO request = new MappingRequestDTO();
		request.setProfileId(4L);

		BusinessException error = assertThrows(BusinessException.class, () -> service.mapKeys(7L, request));
		assertTrue(error.getMessage().contains("乐器档案不存在"), error.getMessage());
	}
}
