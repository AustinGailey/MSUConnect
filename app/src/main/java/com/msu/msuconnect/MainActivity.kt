package com.msu.msuconnect

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem


class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout : DrawerLayout
    private lateinit var mNaviBar : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create toolbar with hamburger menu icon
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_hamburger)
        }

        // create the slide out menu and setup the onClick
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mNaviBar = findViewById(R.id.nav_view)
        mNaviBar.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            mDrawerLayout.closeDrawers()

            // Change fragments here
            when(menuItem.itemId){
                R.id.login -> { updateUI(false)}
                R.id.logout -> { updateUI(true)}
            }
            changeFragment(menuItem.itemId)
            true
        }
        changeFragment(R.id.login)
    }

    fun updateUI(loggedIn : Boolean) {
        if(loggedIn) {
            mNaviBar.menu.findItem(R.id.login).isVisible = false
            mNaviBar.menu.findItem(R.id.logout).isVisible = true
        }
        else {
            mNaviBar.menu.findItem(R.id.login).isVisible = true
            mNaviBar.menu.findItem(R.id.logout).isVisible = false
        }
    }

    public fun changeFragment(newFragmentId : Int)
    {
        when(newFragmentId){
            R.id.login -> { supportFragmentManager.beginTransaction().replace(R.id.fragmentContent, LoginFragment()).commit() }
            R.id.logout -> {
                updateUI(false)
                var loginFrag = LoginFragment()
                var bundle = Bundle()
                bundle.putBoolean("logout", true)
                loginFrag.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContent, loginFrag).commit()
            }
            R.id.profile -> { supportFragmentManager.beginTransaction().replace(R.id.fragmentContent, Profile()).commit() }
            R.id.settings -> { supportFragmentManager.beginTransaction().replace(R.id.fragmentContent, Settings()).commit() }
            R.id.maps -> {
                updateUI(true)
                var mapFrag = MapsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContent, mapFrag).commit()
                mapFrag.getMapAsync(mapFrag)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    public override fun onStart() {
        super.onStart()
    }
}
