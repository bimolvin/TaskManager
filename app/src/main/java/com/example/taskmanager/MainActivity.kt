package com.example.taskmanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Collections


class MainActivity : ComponentActivity() {
    @SuppressLint("ClickableViewAccessibility")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        FORMING RECYCLER VIEW
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val taskList = getTaskList().toMutableList()

        val adapter = TasksAdapter(taskList)
        recyclerView.adapter = adapter

//        BINDING ELEMENTS
        val button: FloatingActionButton = findViewById(R.id.floatingActionButton)
        val textLayout: TextInputLayout = findViewById(R.id.textInputLayout)
        val textField: TextInputEditText = findViewById(R.id.textField)


//        CREATING NEW TASK
        button.setOnClickListener{_ ->
            textLayout.isVisible = true
            val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.scale)
            textLayout.startAnimation(animation)
        }

        textField.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE
                || event.action == KeyEvent.ACTION_DOWN
                && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                if(v.text.isNotEmpty()) {
                    taskList.add(v.text.toString())
                    adapter.notifyItemInserted(taskList.size - 1)
                    recyclerView.smoothScrollToPosition(taskList.size - 1)
                    textField.text = null
                }
                textLayout.isVisible = false
                hideKeyboard()
                true
            } else {
                false
            }
        }

//        HIDING INPUT FIELD
        recyclerView.setOnTouchListener {_, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                textLayout.isVisible = false
                textField.text = null
            }
            false
        }


//        HANDLING TOUCH EVENTS
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT) {

//            swapping tasks
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(taskList, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(taskList, i, i - 1)
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

//            deleting task by swipe
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                if (swipeDir == ItemTouchHelper.LEFT) {
                    taskList.removeAt(viewHolder.bindingAdapterPosition)
                    adapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
                }
            }
        }).attachToRecyclerView(recyclerView)
    }

//    FORMING INITIAL TASK LIST
    private fun getTaskList(): List<String> {
        return this.resources.getStringArray(R.array.task_list).toList()
    }

//    HIDING KEYBOARD
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}
