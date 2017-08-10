package uk.ivanc.archi2017

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*

import uk.ivanc.archi.R
import uk.ivanc.archi2017.model.Repository
import android.arch.lifecycle.ViewModelProviders


class MainActivity : LifecycleActivity() {

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setActionBar(toolbar) TODO

        viewModel = ViewModelProviders
                .of(this, ArchiApplication[this].viewModelFactory)
                .get(MainActivityViewModel::class.java)

        reposRecycleView.setHasFixedSize(true)
        reposRecycleView.layoutManager = LinearLayoutManager(this)
        reposRecycleView.adapter = RepositoryAdapter { repository ->
            startActivity(RepositoryActivity.newIntent(this@MainActivity, repository))
        }

        searchButton.setOnClickListener { loadGithubRepos(editTextUsername.text.toString()) }

        editTextUsername.addTextChangedListener(mHideShowButtonTextWatcher)
        editTextUsername.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val username = editTextUsername.text.toString()
                if (username.isNotEmpty()) {
                    loadGithubRepos(username)
                }
                true
            } else {
                false
            }
        }
    }

    fun loadGithubRepos(username: String) {
        progressBar.visibility = View.VISIBLE
        reposRecycleView.visibility = View.GONE
        infoTextView.visibility = View.GONE
        viewModel.getPublicRepositories(username)
                .observe(this, Observer<List<Repository>> { repositories ->
                    val adapter = reposRecycleView.adapter as RepositoryAdapter
                    adapter.repositories = repositories ?: emptyList()
                    adapter.notifyDataSetChanged()

                    progressBar.visibility = View.GONE
                    if (reposRecycleView.adapter.itemCount > 0) {
                        reposRecycleView.requestFocus()
                        hideSoftKeyboard()
                        reposRecycleView.visibility = View.VISIBLE
                    } else {
                        infoTextView.setText(R.string.text_empty_repos)
                        infoTextView.visibility = View.VISIBLE
                    }
                })
        // TODO error handling
        /*subscription = application.githubService.publicRepositories(username)
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribeOn(application.defaultSubscribeScheduler)
                 .subscribe(object : Subscriber<List<Repository>>() {
                     override fun onCompleted() {

                     }

                     override fun onError(error: Throwable) {
                         Log.e(TAG, "Error loading GitHub repos ", error)
                         progressBar.visibility = View.GONE
                         if (error is HttpException && error.code() == 404) {
                             infoTextView.setText(R.string.error_username_not_found)
                         } else {
                             infoTextView.setText(R.string.error_loading_repos)
                         }
                         infoTextView.visibility = View.VISIBLE
                     }

                     override fun onNext(repositories: List<Repository>) {
                         Log.i(TAG, "Repos loaded " + repositories)
                         val adapter = reposRecycleView.adapter as RepositoryAdapter
                         adapter.repositories = repositories
                         adapter.notifyDataSetChanged()
                     }
                 })*/
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextUsername.windowToken, 0)
    }

    private val mHideShowButtonTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            searchButton.visibility = if (charSequence.isNotEmpty()) View.VISIBLE else View.GONE
        }

        override fun afterTextChanged(editable: Editable) {

        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}
