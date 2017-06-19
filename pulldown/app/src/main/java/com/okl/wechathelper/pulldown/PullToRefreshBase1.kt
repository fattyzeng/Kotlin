package com.okl.wechathelper.pulldown

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/**
 * Created by lenovo on 2017/6/16.
 */
abstract class PullToRefreshBase1<T:View> :LinearLayout,IPullToRefresh<T> {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    enum class Mode{
        PULL_TO_START(0),
        PULL_TO_END(1),
        BOTH(2);
        var index=0
        constructor(int: Int){
            this.index=int
        }

        companion object{

            fun  mapIntValue(int: Int):Mode{
                for (element in Mode.values()){
                    if (element.index==int){
                        return element
                    }
                }
                return getDefault();
            }

            fun getDefault():Mode{
                return BOTH;
            }
        }

    }

    enum class Orientation{
         HORIZONTAL(0),
        VERTICAL(1);
        var index=0
        constructor(int: Int){
            this.index=int
        }

        companion object{

            fun  mapIntValue(int: Int):Orientation{
                for (element in values()){
                    if (element.index==int){
                        return element
                    }
                }
                return getDefault();
            }

            fun getDefault():Orientation{
                return HORIZONTAL;
            }
        }


    }


    enum class AnimationStyle{


    }

    interface  OnRefreshListener{

    }

    interface  OnRefreshListener2{

    }

}