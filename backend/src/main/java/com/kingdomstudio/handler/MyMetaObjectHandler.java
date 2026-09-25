package com.kingdomstudio.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充：插入时填 createTime / updateTime，更新时只改 updateTime。
 *
 * <p>实体上标注 {@code @TableField(fill = FieldFill.INSERT)} 即可生效。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		LocalDateTime now = LocalDateTime.now();
		this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
		this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
	}
}
