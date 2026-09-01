package com.group_finity.mascot.action;

import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.sound.Sounds;
import java.util.ArrayList;
import javax.sound.sampled.Clip;

/**
 * By Kilkakon
 * Go to kilkakon.com for all the best milkshakes and chocolate sundaes
 * Now I'm hungry
 * <p>
 * 静音动作：继承 InstantAction，init 时立即执行一次（apply），无动画帧、不随时间推进
 * （hasNext() 恒为 false，tick() 为空实现）。
 * 用途：
 * 1) 指定 Sound 参数 -> 停止该名字对应的所有正在播放的声音（同一声名的多个音量变体都会被停止）；
 * 2) 未指定 Sound 参数 -> 全局静音，停止全部已加载声音。
 * </p>
 */
@Deprecated
public class Mute extends InstantAction {

    // 要静音的声音名（XML 属性或脚本变量 Sound 提供）；缺省 null 表示执行全局静音（停止全部声音）
    public static final String PARAMETER_SOUND = "Sound";

    private static final String DEFAULT_SOUND = null;

    public Mute( java.util.ResourceBundle schema, final VariableMap params )
    {
        super( schema, params );
    }

    /**
     * 执行静音：
     * 1) 有 Sound 参数：按三级目录（全局 sound / 图像集 sound / 图像集内嵌 img/{ImageSet}/sound）
     *    逐级查找并停止该名字的声音，找到即停止（不再继续下一级）；
     * 2) 无 Sound 参数：声音功能开启时停止全部已加载声音（全局静音）。
     * 注意 getSoundsIgnoringVolume 使用前缀匹配（startsWith），而 SoundLoader 以 "文件名+音量"
     * 为 key 加载声音，因此 Sound 参数会匹配到同一声名的所有音量变体并全部停止。
     */
    @Override
    protected void apply( ) throws VariableException
    {
        String soundName = getSound( );
        if( soundName != null )
        {
            // 第一级：全局 sound 目录（注意此处无路径分隔符，soundName 通常以 "/" 开头，
            // 如 Sound="/ding.wav" -> "./sound/ding.wav"）
            ArrayList<Clip> clips = Sounds.getSoundsIgnoringVolume( "./sound" + soundName );
            if(!clips.isEmpty())
            {
                // 找到匹配声音：停止所有正在播放的变体（未播放的无操作）
                for( Clip clip : clips )
                { 
                    if( clip != null && clip.isRunning( ) ) {
                        clip.stop( );
                    }
                }
            }
            else
            {
                // 第二级：图像集专属 sound 目录 ./sound/{ImageSet}{soundName}
                clips = Sounds.getSoundsIgnoringVolume( "./sound/" + getMascot( ).getImageSet( ) + soundName );
                if(!clips.isEmpty())
                {
                    for( Clip clip : clips )
                    { 
                        if( clip != null && clip.isRunning( ) ) {
                            clip.stop( );
                        }
                    }
                }
                else
                {
                    // 第三级：图像集内嵌 sound 目录 ./img/{ImageSet}/sound{soundName}
                    clips = Sounds.getSoundsIgnoringVolume( "./img/" + getMascot( ).getImageSet( ) + "/sound" + soundName );
                    for( Clip clip : clips )
                    { 
                        if( clip != null && clip.isRunning( ) ) {
                            clip.stop( );
                        }
                    }
                }
            }
        }
        else
        {
            // 全局静音。注意 Sounds.isMuted() 命名有误导性——它读取属性 "Sounds"（默认 "true"），
            // 为 true 表示"声音功能开启"；setMuted(true) 会停止全部已加载声音，
            // 随后的 setMuted(false) 无实际操作（实现中仅 true 分支停止声音）。
            // 组合净效果：仅在声音功能开启时停止所有声音（属性为 false 时不做任何事）。
            if(Sounds.isMuted())
            {
                Sounds.setMuted( true );
                Sounds.setMuted( false );
            }
        }
    }

    private String getSound( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_SOUND ), String.class, DEFAULT_SOUND );
    }
}