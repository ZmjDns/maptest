package com.maptest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.maptest.R;
import com.maptest.adapter.InputTipsAdapter;
import com.maptest.utils.SearchPOIUtil;

import java.util.ArrayList;
import java.util.List;

public class InputTipsActivity extends AppCompatActivity implements
		SearchView.OnQueryTextListener,Inputtips.InputtipsListener,
		AdapterView.OnItemClickListener,
		View.OnClickListener {
	private SearchView mSearchView;
	private ListView mInputListView;
	private List<Tip> mCurrentTipList;
	private InputTipsAdapter mInputTipsAdapter;
	private ImageView mBack;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_tips);

		initSearchView();
		mInputListView = (ListView)findViewById(R.id.inputtip_list);
		//mInputListView.setOnClickListener(this);
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(this);
	}
	private void initSearchView(){
		mSearchView = (SearchView) findViewById(R.id.keyWord);
		mSearchView.setOnQueryTextListener(this);
		//设置SearchView默认展开
		mSearchView.setIconified(false);
		mSearchView.onActionViewExpanded();
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setSubmitButtonEnabled(false);
	}

	@Override
	public void onGetInputtips(List<Tip> tipList, int rCode) {
		if (rCode == 1000){
			mCurrentTipList = tipList;
			List<String> listString = new ArrayList<String>();
			for (int i = 0; i < tipList.size(); i++){
				listString.add(tipList.get(1).getName());
			}
			mInputTipsAdapter = new InputTipsAdapter(getApplicationContext(),mCurrentTipList);
			mInputListView.setAdapter(mInputTipsAdapter);
			mInputTipsAdapter.notifyDataSetChanged();
		}else {
			System.out.println(rCode + rCode+ rCode+ rCode+ rCode);
		}
	}
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		if (mCurrentTipList != null){
			Tip tip =(Tip)adapterView.getItemAtPosition(position);
			Intent intent = new Intent();
			intent.putExtra("ExtraTip",tip);
			this.finish();
		}

	}
	/**
	 * 按下确认键触发，本例为键盘回车或搜索键
	 * @param query
	 * @return
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
		Intent intent = new Intent();
		intent.putExtra("KeyWord",query);
		setResult(ShowMapActivity.RESULT_CODE_KEYWORDS,intent);
		this.finish();
		return false;
	}
	/**
	 * 输入字符变化时触发
	 * @param newText
	 * @return
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		if (!SearchPOIUtil.IsEmptyOrNullString(newText)){
			InputtipsQuery query = new InputtipsQuery(newText,"北京");
			Inputtips inputtips = new Inputtips(this.getApplicationContext(),query);
			inputtips.setInputtipsListener(this);
			inputtips.requestInputtipsAsyn();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.back){
			finish();
		}
	}

}
