
package com.abuhub.tiktokmanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private val accounts = mutableListOf<JSONObject>()
    private lateinit var container: LinearLayout
    private lateinit var username: EditText
    private lateinit var link: EditText
    private lateinit var notes: EditText
    private lateinit var search: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAccounts()
        showScreen()
    }

    private fun loadAccounts() {
        val saved = getSharedPreferences("accounts", MODE_PRIVATE)
            .getString("data", "[]")
        val array = JSONArray(saved)
        for (i in 0 until array.length()) {
            accounts.add(array.getJSONObject(i))
        }
    }

    private fun saveAccounts() {
        val array = JSONArray()
        accounts.forEach { array.put(it) }
        getSharedPreferences("accounts", MODE_PRIVATE)
            .edit().putString("data", array.toString()).apply()
    }

    private fun showScreen() {
        val scroll = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 30)
            setBackgroundColor(Color.rgb(20, 20, 20))
        }
        scroll.addView(container)
        setContentView(scroll)

        val title = TextView(this).apply {
            text = "TIKTOK ACCOUNT MANAGER"
            textSize = 23f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
        }
        container.addView(title)

        username = field("TikTok username")
        link = field("Profile link")
        notes = field("Account notes")

        val add = Button(this).apply {
            text = "ADD ACCOUNT"
            setOnClickListener {
                val name = username.text.toString().trim()
                if (name.isEmpty()) {
                    username.error = "Enter username"
                    return@setOnClickListener
                }
                accounts.add(JSONObject().apply {
                    put("username", name)
                    put("link", link.text.toString())
                    put("notes", notes.text.toString())
                })
                saveAccounts()
                username.text.clear()
                link.text.clear()
                notes.text.clear()
                showScreen()
            }
        }
        container.addView(add)

        search = field("Search accounts")
        search.addTextChangedListener(object :
            android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) { showAccounts(s.toString()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        showAccounts("")
    }

    private fun field(hintText: String): EditText {
        val input = EditText(this).apply {
            hint = hintText
            setHintTextColor(Color.LTGRAY)
            setTextColor(Color.WHITE)
        }
        container.addView(input)
        return input
    }

    private fun showAccounts(filter: String) {
        val old = container.findViewWithTag<LinearLayout>("accountList")
        if (old != null) container.removeView(old)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = "accountList"
        }

        container.addView(list)

        val heading = TextView(this).apply {
            text = "SAVED ACCOUNTS (${accounts.size})"
            textSize = 18f
            setTextColor(Color.YELLOW)
        }
        list.addView(heading)

        accounts.forEachIndexed { index, account ->
            val name = account.optString("username")
            if (!name.contains(filter, true)) return@forEachIndexed

            val item = TextView(this).apply {
                text = "$name\n${account.optString("link")}\n${account.optString("notes")}"
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(12, 20, 12, 20)
                setBackgroundColor(Color.DKGRAY)
            }
            list.addView(item)

            val delete = Button(this).apply {
                text = "DELETE $name"
                setOnClickListener {
                    accounts.removeAt(index)
                    saveAccounts()
                    showScreen()
                }
            }
            list.addView(delete)
        }
    }
}
