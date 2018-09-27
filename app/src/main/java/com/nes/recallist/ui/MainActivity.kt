
package com.nes.recallist.ui

import android.os.Bundle
import com.google.api.services.drive.model.File
import com.nes.recallist.R
import com.nes.recallist.ui.files.FilesFragment

import com.nes.transfragment.BaseTransActivity

class MainActivity : BaseTransActivity() {

  var selectedFile: File? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, FilesFragment())
            .commit()
  }
}
