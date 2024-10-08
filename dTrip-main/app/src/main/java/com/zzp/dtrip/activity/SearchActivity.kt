package com.zzp.dtrip.activity

import android.app.ActivityOptions
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zzp.dtrip.R
import com.zzp.dtrip.adapter.AddressAdapter
import com.zzp.dtrip.data.*
import com.zzp.dtrip.fragment.TripFragment
import com.zzp.dtrip.util.TencentAppService
import com.zzp.dtrip.util.TencentRetrofitManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    companion object {
        val resultList = ArrayList<BigData>()
    }

    private lateinit var searchEdit: EditText

    private lateinit var recyclerView: RecyclerView

    private var adapter: AddressAdapter? = null

    private var keyword = ""

    private val KEY = "F5IBZ-US3CW-3JIRY-OKBB5-TUMWV-S7BVZ"

    private val TAG = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        findViewById()
        initRecyclerView()
        initEdit()
        getExplore()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        // 使 EditText 默认获得焦点
        Handler(mainLooper).postDelayed({
            searchEdit.requestFocus()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT)
        }, 400)
    }

    private fun findViewById() {
        searchEdit = findViewById(R.id.search_edit)
        recyclerView = findViewById(R.id.address_recycler)
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        if (TripFragment.position == -1) {
            resultList.clear()
        }
        adapter = AddressAdapter(this, resultList)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun initEdit() {
        if(TripFragment.position != -1) {
            keyword = resultList[TripFragment.position].title
            searchEdit.setText(keyword)
            getSuggestion()
        }
        // 监听 EditText 以实现自动搜索
        searchEdit.addTextChangedListener { text: Editable? ->
            if (!text?.toString().isNullOrEmpty()) {
                keyword = text.toString()
                getSuggestion()
            }
        }

    }

    private fun getSuggestion() {
        val appService = TencentRetrofitManager.create<TencentAppService>()
        val task = appService.getSuggestion(keyword, TripFragment.city, KEY)
        Log.d(TAG, "getSuggestion: " + TripFragment.city)
        task.enqueue(object : Callback<SuggestionResult>{
            override fun onResponse(call: Call<SuggestionResult>,
                                    response: Response<SuggestionResult>) {
                response.body()?.apply {
                    if (this.status == 0) {
                        if (resultList.size != 0) {
                            resultList.clear()
                        }
                        for (obj in this.data) {
                            val objs = BigData(obj)
                            resultList.add(objs)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                    else {
                        Toast.makeText(this@SearchActivity, "请求错误",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<SuggestionResult>, t: Throwable) {
                Log.d(TAG, "onFailure ==> $t")
            }
        })
    }

    private fun getExplore() {
        val appService = TencentRetrofitManager.create<TencentAppService>()
        val boundary = "nearby(${TripFragment.lat},${TripFragment.lng},1000)"
        val task = appService.getExplore(boundary, KEY)
        task.enqueue(object : Callback<ExploreResult>{
            override fun onResponse(call: Call<ExploreResult>,
                                    response: Response<ExploreResult>) {
                response.body()?.apply {
                    if (this.status == 0) {
                        if (resultList.size != 0) {
                            resultList.clear()
                        }
                        for (obj in this.data) {
                            val objs = BigData(obj)
                            resultList.add(objs)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                    else {
                        Toast.makeText(this@SearchActivity, "请求错误",
                            Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "onResponse: ${this.status}" )
                    }
                }
            }

            override fun onFailure(call: Call<ExploreResult>, t: Throwable) {
                Log.d(TAG, "onFailure ==> $t")
            }
        })
    }

}