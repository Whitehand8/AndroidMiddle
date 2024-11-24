package com.example.godparttimejob.ui.settingcompany

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.godparttimejob.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class SettingCompanyFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var editCompanyName: EditText
    private lateinit var editCompanyDescription: EditText
    private lateinit var imageLargeCompany: ImageView
    private lateinit var buttonUploadLargeImage: Button
    private lateinit var imageIconCompany: ImageView
    private lateinit var buttonUploadIconImage: Button
    private lateinit var checkboxRecruiting: CheckBox
    private lateinit var buttonRegisterCompany: Button

    private var largeImageUri: Uri? = null
    private var iconImageUri: Uri? = null
    private var largeImageUrl: String? = null
    private var iconImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting_company, container, false)

        // Firebase 초기화
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // View 초기화
        editCompanyName = view.findViewById(R.id.editCompanyName)
        editCompanyDescription = view.findViewById(R.id.editCompanyDescription)
        imageLargeCompany = view.findViewById(R.id.imageLargeCompany)
        buttonUploadLargeImage = view.findViewById(R.id.buttonUploadLargeImage)
        imageIconCompany = view.findViewById(R.id.imageIconCompany)
        buttonUploadIconImage = view.findViewById(R.id.buttonUploadIconImage)
        checkboxRecruiting = view.findViewById(R.id.checkboxRecruiting)
        buttonRegisterCompany = view.findViewById(R.id.buttonRegisterCompany)

        // 큰 이미지 업로드 버튼
        buttonUploadLargeImage.setOnClickListener { selectImage(true) }

        // 아이콘 이미지 업로드 버튼
        buttonUploadIconImage.setOnClickListener { selectImage(false) }

        // 회사 등록 버튼
        buttonRegisterCompany.setOnClickListener { registerCompany() }

        return view
    }

    // 이미지 선택
    private fun selectImage(isLargeImage: Boolean) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val launcher = if (isLargeImage) largeImagePickerLauncher else iconImagePickerLauncher
        launcher.launch(intent)
    }

    // 큰 이미지 선택 결과 처리
    private val largeImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            largeImageUri = result.data?.data
            imageLargeCompany.setImageURI(largeImageUri)
        }
    }

    // 아이콘 이미지 선택 결과 처리
    private val iconImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            iconImageUri = result.data?.data
            imageIconCompany.setImageURI(iconImageUri)
        }
    }

    // Firebase Storage에 이미지 업로드
    private fun uploadImageToStorage(uri: Uri?, isLargeImage: Boolean, onComplete: (String?) -> Unit) {
        if (uri == null) {
            onComplete(null)
            return
        }

        val filename = UUID.randomUUID().toString() + ".jpg"
        val ref = storage.reference.child("company_images/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    onComplete(url.toString())
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    // 회사 정보 등록
    private fun registerCompany() {
        val name = editCompanyName.text.toString().trim()
        val description = editCompanyDescription.text.toString().trim()
        val isRecruiting = checkboxRecruiting.isChecked

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력하세요!", Toast.LENGTH_SHORT).show()
            return
        }

        // 이미지 업로드
        uploadImageToStorage(largeImageUri, true) { largeUrl ->
            if (largeUrl == null) {
                Toast.makeText(requireContext(), "큰 이미지 업로드 실패!", Toast.LENGTH_SHORT).show()
                return@uploadImageToStorage
            }
            largeImageUrl = largeUrl

            uploadImageToStorage(iconImageUri, false) { iconUrl ->
                if (iconUrl == null) {
                    Toast.makeText(requireContext(), "아이콘 이미지 업로드 실패!", Toast.LENGTH_SHORT).show()
                    return@uploadImageToStorage
                }
                iconImageUrl = iconUrl

                // Firestore에 데이터 저장
                val companyData = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "largeImageUrl" to largeImageUrl,
                    "iconImageUrl" to iconImageUrl,
                    "isRecruiting" to isRecruiting,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("companies")
                    .add(companyData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "회사 등록 성공!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "회사 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
