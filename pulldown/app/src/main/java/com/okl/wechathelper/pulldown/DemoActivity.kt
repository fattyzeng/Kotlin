package com.okl.wechathelper.pulldown

import android.app.ListActivity
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.okl.wechathelper.R
import com.okl.wechathelper.pulldown.PullToRefreshBase.OnLastItemVisibleListener
import java.util.*


/**
 * Created by lenovo on 2017/6/18.
 */
class DemoActivity: ListActivity() {
    val MENU_MANUAL_REFRESH = 0
    val MENU_DISABLE_SCROLL = 1
    val MENU_SET_MODE = 2
    val MENU_DEMO = 3

    private var mListItems: LinkedList<String>? = null
    private var mPullRefreshListView: PullToRefreshListView? = null
    private var mAdapter: ArrayAdapter<String>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        mPullRefreshListView = findViewById(R.id.pull_refresh_list) as PullToRefreshListView

        mPullRefreshListView!!.setOnRefreshListener(object : PullToRefreshBase.OnRefreshListener<ListView> {
            override fun onRefresh(refreshView: PullToRefreshBase<ListView>) {
                val label = DateUtils.formatDateTime(applicationContext, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL)

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label)

                GetDataTask().execute()
            }
        })

        mPullRefreshListView!!.setOnLastItemVisibleListener(object : OnLastItemVisibleListener {

            override fun onLastItemVisible() {
                Toast.makeText(this@DemoActivity, "列表结尾", Toast.LENGTH_SHORT).show()
            }
        })

        var actualListView = mPullRefreshListView!!.getRefreshableView() as ListView


        mListItems = LinkedList<String>()
        mListItems!!.addAll(mStrings)

        mAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListItems)


        actualListView.setAdapter(mAdapter)

    }

    private inner class GetDataTask : AsyncTask<Void, Void, List<String>>() {

        override fun doInBackground(vararg params: Void): List<String> {

            try {
                Thread.sleep(4000)
            } catch (e: InterruptedException) {
            }

            return mStrings
        }

        override fun onPostExecute(result: List<String>) {
            mListItems!!.addFirst("添加刷新...")
            mAdapter!!.notifyDataSetChanged()
            // Call onRefreshComplete when the list has been refreshed.
            mPullRefreshListView!!.onRefreshComplete()

            super.onPostExecute(result)
        }
    }






    private var mStrings = listOf<String>("启子",
    "玛莉亚Marie",
    "立花美凉",
    "辰巳唯",
    "堀口奈津美",
    "波多野结衣",
    "村上里沙",
    "泽木树里",
    "月野姬",
    "茉莉花绫波优",
    "叶志穗",
    "圣橘未稀",
    "岬里沙",
    "爱泽莲",
    "伊藤青叶",
    "明佐奈",
    "原更纱",
    "藤井雪莉",
    "高濑七海",
    "小峰日向",
    "南沙也香",
    "初美理音",
    "早乙女露依",
    "横山美雪",
    "相田纱耶香",
    "筱原凉",
    "翼裕香",
    "KYOKO",
    "田中亚弥",
    "叶山润子",
    "羽田未来",
    "花鸟丽",
    "赤西凉",
    "羽田爱",
    "仁科沙也加",
    "大槻响",
    "北原夏美",
    "坂田美影",
    "鮎川奈绪",
    "长泽梓",
    "白鸟凉子",
    "大泽佳那",
    "若宫莉那",
    "Hitomi",
    "飞鸟伊央",
    "伊东遥",
    "绫濑美音",
    "樱井莉亚",
    "明日花绮罗",
    "雨宫琴音",
    "花井美沙",
    "铃香音色",
    "青山由衣",
    "羽月希",
    "心有花",
    "莲井志帆"
    )
}