package com.jet.im.kit.activities.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.sendbird.android.message.Form
import com.sendbird.android.message.FormField
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.databinding.SbViewFormFieldBinding
import com.jet.im.kit.internal.extensions.lastValidation

internal class FormFieldAdapter : BaseAdapter<FormField, BaseViewHolder<FormField>>() {
    private val formFields: MutableList<FormField> = mutableListOf()

    fun isSubmittable(): Boolean {
        return formFields.all { it.isSubmittable }
    }

    fun updateValidation() {
        formFields.forEachIndexed { index, formField ->
            val lastValidation = formField.lastValidation
            val validation = formField.isSubmittable
            formField.lastValidation = validation
            if (lastValidation != validation) {
                notifyItemChanged(index)
            }
        }
    }

    fun setFormFields(form: Form) {
        val newFormFields = if (form.isSubmitted) {
            form.formFields.filter { it.answer != null }
        } else {
            form.formFields
        }
        val diffResult = DiffUtil.calculateDiff(FormFieldDiffCallback(formFields, newFormFields))
        formFields.clear()
        formFields.addAll(newFormFields)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<FormField> {
        return FormFieldViewHolder(
            SbViewFormFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return formFields.size
    }

    override fun getItem(position: Int): FormField {
        return formFields[position]
    }

    override fun getItems(): List<FormField> {
        return formFields.toList()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FormField>, position: Int) {
        holder.bind(getItem(position))
    }

    internal class FormFieldViewHolder(
        val binding: SbViewFormFieldBinding
    ) : BaseViewHolder<FormField>(binding.root) {
        override fun bind(item: FormField) {
            binding.formFieldView.drawFormField(item)
        }
    }

    private class FormFieldDiffCallback(
        private val oldList: List<FormField>,
        private val newList: List<FormField>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.key == newItem.key &&
                oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
