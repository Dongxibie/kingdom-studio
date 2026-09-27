# Kingdom Studio · 演奏窗口守护（只读）
#
# 用途：本机演奏时，Java 侧需要两件纯 Java 读不到的信息：
#   1) 当前前台窗口的标题 —— 用来守住「只对用户确认过的窗口生效」；
#   2) ESC 是否被按下 —— 用来实现随时可用的急停。
#
# 这个脚本只做这两件事，而且只读：
#   · 只查询 ESC 一个虚拟键的状态，不读取、不记录、不保存任何其他按键；
#   · 不写文件、不联网、不改系统设置、不注入任何进程；
#   · 输出只有三类行：READY / WINDOW <标题> / ESC。
#
# 想核对它到底做了什么，直接看这个文件即可（一共几十行）。
# 由后端在执行本机演奏时启动，会话结束时随进程一起关闭。

param(
    [int]$IntervalMs = 120
)

$ErrorActionPreference = 'Stop'

Add-Type -Namespace Ks -Name Native -MemberDefinition @'
[DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
[DllImport("user32.dll", CharSet = CharSet.Unicode)] public static extern int GetWindowTextW(IntPtr hWnd, System.Text.StringBuilder text, int max);
[DllImport("user32.dll")] public static extern short GetAsyncKeyState(int vKey);
'@

function Get-ForegroundTitle {
    $handle = [Ks.Native]::GetForegroundWindow()
    if ($handle -eq [IntPtr]::Zero) { return '' }
    $buffer = New-Object System.Text.StringBuilder 512
    [void][Ks.Native]::GetWindowTextW($handle, $buffer, $buffer.Capacity)
    return $buffer.ToString()
}

$VK_ESCAPE = 0x1B

# 立刻上报一次当前窗口，Java 侧据此显示「执行前的目标窗口」
Write-Output 'READY'
Write-Output ("WINDOW " + (Get-ForegroundTitle))

$last = $null
while ($true) {
    # 急停：只查询 ESC 这一个键的按下状态（最高位为 1 表示当前按下）
    $state = [Ks.Native]::GetAsyncKeyState($VK_ESCAPE)
    if (($state -band 0x8000) -ne 0) {
        Write-Output 'ESC'
        break
    }

    $title = Get-ForegroundTitle
    if ($title -ne $last) {
        Write-Output ("WINDOW " + $title)
        $last = $title
    }

    Start-Sleep -Milliseconds $IntervalMs
}
