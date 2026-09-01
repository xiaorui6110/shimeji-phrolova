package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.hotspot.Hotspot;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.image.ImagePairLoader.Filter;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.sound.SoundLoader;
/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 动画构建器：把 actions.xml 中的一个 <Animation> 节点解析为运行时 Animation 对象。
 * 一个动画由四要素构成：播放条件（Condition 表达式，缺省恒真）、关键帧列表（Pose）、
 * 点击热点列表（Hotspot）、转身标志（IsTurn，供 Move 动作按 turning 状态匹配转身动画）。
 * 图片与声音在构造阶段即预加载（启动期一次性完成），构建后 Animation 只负责按时间播放。
 * </p>
 */

public class AnimationBuilder {

    private static final Logger log = Logger.getLogger(AnimationBuilder.class.getName( ) );
    private final String condition;
    private String imageSet = "";
    private final List<Pose> poses = new ArrayList<>();
    private final List<Hotspot> hotspots = new ArrayList<>();
    private final ResourceBundle schema;
    private final String turn;

    /**
     * 解析动画节点：
     * 1) 记录图像集前缀、Condition 与 IsTurn 属性；
     * 2) 遍历子元素 <Pose> -> loadPose（解析帧并预加载图片/声音）；
     * 3) 遍历子元素 <Hotspot> -> loadHotspot（解析点击区域）。
     */
    public AnimationBuilder( final ResourceBundle schema, final Entry animationNode, final String imageSet ) throws IOException
    {
        if(!imageSet.isEmpty()) {
            this.imageSet = "/"+imageSet;
        }

        this.schema = schema;
        this.condition = animationNode.getAttribute( schema.getString( "Condition" ) ) == null ? "true" : animationNode.getAttribute( schema.getString( "Condition" ) );
        this.turn = animationNode.getAttribute( schema.getString( "IsTurn" ) ) == null ? "false" : animationNode.getAttribute( schema.getString( "IsTurn" ) );

        log.log( Level.INFO, "Start Reading Animations" );

        for( final Entry frameNode : animationNode.selectChildren( schema.getString( "Pose" ) ) )
        {
            poses.add( loadPose( frameNode ) );
        }

        for( final Entry frameNode : animationNode.selectChildren( schema.getString( "Hotspot" ) ) )
        {
            hotspots.add( loadHotspot( frameNode ) );
        }
        
        log.log( Level.INFO, "Animations Finished Loading" );
    }

    /**
     * 解析一个 <Pose> 帧：
     * 1) 读取图片属性：Image（左向）/ImageRight（右向，可缺省，缺省时右图自动翻转左图），
     *    均拼上图像集前缀；ImageAnchor 为"x,y"锚点（图片与翻转后的对称参考点）；
     * 2) 读取 Velocity（"x,y"每帧位移）与 Duration（帧时长毫秒）——两者必填，无缺省值；
     * 3) 读取 Sound/Volume（可缺省）：按三级目录探测声音文件并预加载；
     * 4) 图片存在时调用 ImagePairLoader.load 预加载（启动期一次性完成，失败抛出带图片名的异常）。
     */
    private Pose loadPose( final Entry frameNode ) throws IOException
    {
        // 图片路径 = 图像集前缀 + 属性值，如 "/Phrolova" + "/WalkLeft.png" -> "/Phrolova/WalkLeft.png"
        final String imageText = frameNode.getAttribute( schema.getString( "Image" ) ) != null ? imageSet+frameNode.getAttribute( schema.getString( "Image" ) ) : null;
        final String imageRightText = frameNode.getAttribute( schema.getString( "ImageRight" ) ) != null ? imageSet+frameNode.getAttribute( schema.getString( "ImageRight" ) ) : null;
        final String anchorText = frameNode.getAttribute( schema.getString( "ImageAnchor" ) ) != null ? frameNode.getAttribute( schema.getString( "ImageAnchor" ) ) : null;
        final String moveText = frameNode.getAttribute( schema.getString( "Velocity" ) );
        final String durationText = frameNode.getAttribute( schema.getString( "Duration" ) );
        String soundText = frameNode.getAttribute( schema.getString( "Sound" ) ) != null ? frameNode.getAttribute( schema.getString( "Sound" ) ) : null;
        final String volumeText = frameNode.getAttribute( schema.getString( "Volume" ) ) != null ? frameNode.getAttribute( schema.getString( "Volume" ) ) : "0";

        // 全局图像参数：不透明度 Opacity（缺省 1.0）、缩放 Scaling（缺省 1.0）、
        // 缩放滤镜 Filter（缺省 false=最近邻；"true"/"hqx"=HQX；"bicubic"=双三次）
        final double opacity = Double.parseDouble( Main.getInstance( ).getProperties( ).getProperty( "Opacity", "1.0" ) );
        final double scaling = Double.parseDouble( Main.getInstance( ).getProperties( ).getProperty( "Scaling", "1.0" ) );
        
        String filterText = Main.getInstance( ).getProperties( ).getProperty( "Filter", "false" );
        Filter filter = Filter.NEAREST_NEIGHBOUR;
        if( filterText.equalsIgnoreCase( "true" ) || filterText.equalsIgnoreCase( "hqx" ) ) {
            filter = Filter.HQX;
        } else if( filterText.equalsIgnoreCase( "bicubic" ) ) {
            filter = Filter.BICUBIC;
        }

        if( imageText != null )
        {
            try
            {
                // 解析锚点坐标（"x,y"）并预加载图片对（ImagePairs 缓存以 左图路径+右图路径 为 key）
                final String[] anchorCoordinates = anchorText.split( "," );
                final Point anchor = new Point( Integer.parseInt( anchorCoordinates[ 0 ] ), Integer.parseInt( anchorCoordinates[ 1 ] ) );

                ImagePairLoader.load( imageText, imageRightText, anchor, scaling, filter, opacity );
            }
            catch( Exception e )
            {
                // 图片加载失败：抛出带文件名的异常终止启动（含 ImageAnchor 缺失导致的解析异常）
                String error = imageText;
                if( imageRightText != null ) {
                    error += ", " + imageRightText;
                }
                log.log( Level.SEVERE, "Failed to load image: {0}", error );
                throw new IOException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedLoadImageErrorMessage" ) + " " + error );
            }
        }

        // 位移按缩放比例换算；非零位移缩放后不足 1 像素时归一为 ±1，保证缩放后仍有最小位移
        final String[] moveCoordinates = moveText.split( "," );
        int moveX = Integer.parseInt( moveCoordinates[ 0 ] );
        int moveY = Integer.parseInt( moveCoordinates[ 1 ] );
        moveX = Math.abs( moveX ) > 0 && Math.abs( moveX * scaling ) < 1 ? ( moveX > 0 ? 1 : -1 ) : (int)Math.round( moveX * scaling );
        moveY = Math.abs( moveY ) > 0 && Math.abs( moveY * scaling ) < 1 ? ( moveY > 0 ? 1 : -1 ) : (int)Math.round( moveY * scaling );
        final Point move = new Point( moveX, moveY );
        final int duration = Integer.parseInt( durationText );

        if( soundText != null )
        {
            try
            {
                // 声音文件三级目录探测：全局 ./sound -> 图像集专属 ./sound/{ImageSet} -> 图像集内嵌 ./img/{ImageSet}/sound
                if( new File( "./sound" + soundText ).exists( ) ) {
                    soundText = "./sound" + soundText;
                } else if( new File( "./sound" + imageSet + soundText ).exists( ) ) {
                    soundText = "./sound" + imageSet + soundText;
                } else {
                    soundText = "./img" + imageSet + "/sound" + soundText;
                }

                // SoundLoader 以"路径+音量"为 key 缓存，故加载后把音量拼进 soundText 供 Pose 引用
                SoundLoader.load( soundText, Float.parseFloat( volumeText ) );
                soundText += Float.parseFloat( volumeText );
            }
            catch( Exception e )
            {
                log.log( Level.SEVERE, "Failed to load sound: {0}", soundText );
                throw new IOException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedLoadSoundErrorMessage" ) + soundText );
            }
        }

        // 组装关键帧（image 为 null 时该帧无图，仅用于位移/声音，但仍需 Velocity 与 Duration）
        final Pose pose = new Pose( imageText, imageRightText, move.x, move.y, duration, soundText);

        log.log( Level.INFO, "ReadPosition({0})" , pose );

        return pose;
    }

    /**
     * 解析一个 <Hotspot> 点击区域：
     * Shape 指定形状（Rectangle/Ellipse），Origin 为"x,y"起点，Size 为"w,h"尺寸，
     * 两者均按全局缩放比例 Scaling 换算；Behaviour 为点击后要切换的行为名。
     * 运行时 Mascot 按下检测：命中区域（含面向右时的镜像变换）即切换行为。
     */
    private Hotspot loadHotspot( final Entry frameNode ) throws IOException
    {
        final String shapeText = frameNode.getAttribute( schema.getString( "Shape" ) );
        final String originText = frameNode.getAttribute( schema.getString( "Origin" ) );
        final String sizeText = frameNode.getAttribute( schema.getString( "Size" ) );
        final String behaviourText = frameNode.getAttribute( schema.getString( "Behaviour" ) );
        final double scaling = Double.parseDouble( Main.getInstance( ).getProperties( ).getProperty( "Scaling", "1.0" ) );

        // 起点与尺寸按缩放比例换算（四舍五入到整数像素）
        final String[ ] originCoordinates = originText.split( "," );
        final String[ ] sizeCoordinates = sizeText.split( "," );
        
        final Point origin = new Point( (int)Math.round( Integer.parseInt( originCoordinates[ 0 ] ) * scaling ),
                                        (int)Math.round( Integer.parseInt( originCoordinates[ 1 ] ) * scaling ) );
        final Dimension size = new Dimension( (int)Math.round( Integer.parseInt( sizeCoordinates[ 0 ] ) * scaling ), 
                                              (int)Math.round( Integer.parseInt( sizeCoordinates[ 1 ] ) * scaling ) );
        
        // 形状分派：仅支持矩形与椭圆，其余抛"不支持的形状"异常终止启动
        Shape shape;
        if( shapeText.equalsIgnoreCase( "Rectangle" ) )
        {
            shape = new Rectangle( origin, size );
        }
        else if( shapeText.equalsIgnoreCase( "Ellipse" ) )
        {
            shape = new Ellipse2D.Float( origin.x, origin.y, size.width, size.height );
        }
        else
        {
            log.log( Level.SEVERE, "Failed to load hotspot shape: {0}", shapeText );
            throw new IOException( Main.getInstance( ).getLanguageBundle( ).getString( "HotspotShapeNotSupportedErrorMessage" ) + " " + shapeText );
        }

        // 组装热点：行为名可为 null（仅拦截拖拽、不切换行为，见 UserBehavior）
        final Hotspot hotspot = new Hotspot( behaviourText, shape );

        log.log( Level.INFO, "ReadHotSpot({0})", hotspot );

        return hotspot;
    }

    /**
     * 组装 Animation 对象：
     * Condition 字符串经 Variable.parse 解析为可求值表达式（${...} 变量引用），
     * 连同关键帧、热点、转身标志（IsTurn，供 Move 动作的转身动画匹配）一并封装。
     * 条件表达式解析失败时转换为 AnimationInstantiationException。
     */
    public Animation buildAnimation( ) throws AnimationInstantiationException
    {
        try
        {
            return new Animation( Variable.parse( condition ), poses.toArray( new Pose[ 0 ] ), hotspots.toArray( new Hotspot[ 0 ] ), Boolean.parseBoolean( turn ) );
        }
        catch( final VariableException e )
        {
            throw new AnimationInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedConditionEvaluationErrorMessage" ), e );
        }
    }
}
