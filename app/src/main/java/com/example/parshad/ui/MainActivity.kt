package com.example.parshad.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.parshad.MyApplication
import com.example.parshad.R
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.databinding.ActivityMainBinding
import com.example.parshad.ui.viewModels.MainViewModel
import com.example.parshad.ui.viewModels.MainViewModelFactory


class MainActivity : AppCompatActivity() {

    lateinit var authDatabase: AuthDatabase
    lateinit var mainViewModel: MainViewModel
    lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.toolbar_drawable))
        supportActionBar?.show()
        authDatabase = AuthDatabase()
        val mainViewModelFactory = MainViewModelFactory(application as MyApplication, authDatabase)
        mainViewModel = ViewModelProvider(this, mainViewModelFactory)[MainViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        // using toolbar as ActionBar

        // using toolbar as ActionBar
        setUserDetails()
        drawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.foodNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)
        setNavHeader()
    }

    private fun setNavHeader() {
        mainViewModel.userImage.observe(this) {
            if (it!=null&&it.isNotBlank() && it.isNotEmpty()) {
                binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.nav_user_profile)
                    .setImageBitmap(getUserImage(it))
            }

        }
        mainViewModel.userName.observe(this) {
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.nav_user_name).text = it?:""
        }
        mainViewModel.userWard.observe(this) {
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.nav_user_ward).text = it?:""
        }

        binding.navView.setNavigationItemSelectedListener { item->
            if(item.itemId==R.id.nav_logout)
            {
                binding.drawerLayout.close()
                logout()
                true
            }
            if(item.itemId==R.id.myProblemFragment){
                binding.drawerLayout.close()
                findNavController(R.id.foodNavHostFragment).navigateUp()
                findNavController(R.id.foodNavHostFragment).navigate(R.id.myProblemFragment)
                true
            }
            if(item.itemId==R.id.nav_settings){
                binding.drawerLayout.close()
                findNavController(R.id.foodNavHostFragment).navigateUp()
                findNavController(R.id.foodNavHostFragment).navigate(R.id.settingsInFragment)
            }
            false
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {

        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    }

    private fun setUserDetails() {
        mainViewModel.getNameFromDataStore()
        mainViewModel.getUserPhoneFromDataStore()
        mainViewModel.getUserWardFromDataStore()
        mainViewModel.getUserImageFromDataStore()
        mainViewModel.getUserRoleFromDataStore()
        mainViewModel.getAadhaarFromDataStore()
        mainViewModel.getUserGenderFromDataStore()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.foodNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.side_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun logout(){
        mainViewModel.setIsSignedInFromDataStore(false)
        authDatabase.auth.signOut()
        Intent(this, AuthActivity::class.java).apply {
            startActivity(this)
            this@MainActivity.finish()
        }
        Toast.makeText(this,"Logout",Toast.LENGTH_SHORT).show()
    }


}