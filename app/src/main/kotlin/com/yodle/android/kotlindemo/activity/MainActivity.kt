package com.yodle.android.kotlindemo.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.jakewharton.rxbinding.widget.RxTextView
import com.yodle.android.kotlindemo.MainApp
import com.yodle.android.kotlindemo.R
import com.yodle.android.kotlindemo.adapter.RepositoryAdapter
import com.yodle.android.kotlindemo.extension.hide
import com.yodle.android.kotlindemo.extension.show
import com.yodle.android.kotlindemo.model.Repository
import com.yodle.android.kotlindemo.service.GitHubService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity(), Observer<List<Repository>> {

    @Inject lateinit var gitHubService: GitHubService

    lateinit var repositoryAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MainApp.graph.inject(this)

        setSupportActionBar(toolbar)
        setUpRecyclerView()
        setUpSearchView()
    }

    override fun onCompleted() {
    }

    override fun onError(e: Throwable) {
        Timber.e(e, "Failed to load repositories")
        Toast.makeText(this, "Error performing search, disabling search field", Toast.LENGTH_SHORT).show()
        mainResultsSpinner.hide()
    }

    override fun onNext(repositories: List<Repository>) {
        mainResultsSpinner.hide()
        repositoryAdapter.repositories = repositories
        repositoryAdapter.notifyDataSetChanged()
    }

    fun setUpRecyclerView() {
        repositoryAdapter = RepositoryAdapter(this)
        mainResultsRecycler.adapter = repositoryAdapter
        mainResultsRecycler.layoutManager = LinearLayoutManager(this)
    }

    fun setUpSearchView() {
        val searchEditText = mainSearchCardView.getEditText()
        searchEditText.setText("kotlin")
        searchEditText.setSelection(searchEditText.getText().length);
        searchEditText.setHint(R.string.search_repositories)
        RxTextView.textChanges(searchEditText)
                .doOnNext { mainResultsSpinner.show() }
                .sample(1, TimeUnit.SECONDS)
                .switchMap { gitHubService.searchRepositories(it.toString()).subscribeOn(Schedulers.io()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)
    }
}
