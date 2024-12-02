package com.example.godparttimejob.ui.settingcompany

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.godparttimejob.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
//언제나 여러분과 24시간 함께하는 GU입니다!
class SettingCompanyFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var editCompanyName: EditText
    private lateinit var editCompanyDescription: EditText
    private lateinit var editCompanyAddress: EditText // 주소 입력 필드
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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        editCompanyName = view.findViewById(R.id.editCompanyName)
        editCompanyDescription = view.findViewById(R.id.editCompanyDescription)
        editCompanyAddress = view.findViewById(R.id.editCompanyAddress) // 주소 필드 초기화
        imageLargeCompany = view.findViewById(R.id.imageLargeCompany)
        buttonUploadLargeImage = view.findViewById(R.id.buttonUploadLargeImage)
        imageIconCompany = view.findViewById(R.id.imageIconCompany)
        buttonUploadIconImage = view.findViewById(R.id.buttonUploadIconImage)
        checkboxRecruiting = view.findViewById(R.id.checkboxRecruiting)
        buttonRegisterCompany = view.findViewById(R.id.buttonRegisterCompany)

        buttonUploadLargeImage.setOnClickListener { selectImage(true) }
        buttonUploadIconImage.setOnClickListener { selectImage(false) }

        checkIfAdmin() // 운영자 여부 확인

        return view
    }

    private fun checkIfAdmin() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role == "admin") {
                    buttonRegisterCompany.setOnClickListener { registerCompany() }
                } else {
                    Toast.makeText(requireContext(), "운영자만 접근할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "운영자 권한 확인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
    }

    private fun selectImage(isLargeImage: Boolean) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val launcher = if (isLargeImage) largeImagePickerLauncher else iconImagePickerLauncher
        launcher.launch(intent)
    }

    private val largeImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            largeImageUri = result.data?.data
            imageLargeCompany.setImageURI(largeImageUri)
        }
    }

    private val iconImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            iconImageUri = result.data?.data
            imageIconCompany.setImageURI(iconImageUri)
        }
    }

    private fun resizeImageWithAspect(
        uri: Uri?,
        maxWidth: Int,
        maxHeight: Int,
        isLargeImage: Boolean,
        onComplete: (ByteArray?) -> Unit
    ) {
        if (uri == null) {
            onComplete(null)
            return
        }

        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // 가로와 세로의 비율 유지하며 리사이징
            val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height
            val targetWidth: Int
            val targetHeight: Int

            if (isLargeImage) {
                // 큰 이미지는 가로 비중을 높여서 4:3 비율로 조정
                targetWidth = maxWidth
                targetHeight = (maxWidth / aspectRatio).toInt()
            } else {
                // 아이콘 이미지는 정사각형으로 조정
                targetWidth = maxWidth
                targetHeight = maxHeight
            }

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            // Bitmap을 ByteArray로 변환
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            onComplete(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }

    private fun resizeImage(
        uri: Uri?,
        maxWidth: Int,
        maxHeight: Int,
        isLargeImage: Boolean,
        onComplete: (ByteArray?) -> Unit
    ) {
        if (uri == null) {
            onComplete(null)
            return
        }

        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // 가로와 세로 비율 유지
            val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height
            val targetWidth: Int
            val targetHeight: Int

            if (isLargeImage) {
                // 큰 이미지는 4:3 비율
                targetWidth = maxWidth
                targetHeight = (maxWidth * 3) / 4 // 4:3 비율
            } else {
                // 아이콘은 정사각형
                targetWidth = maxWidth
                targetHeight = maxHeight
            }

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            // Bitmap을 ByteArray로 변환
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            onComplete(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }

    private fun uploadImageToStorage(uri: Uri?, isIcon: Boolean, onComplete: (String?) -> Unit) {
        if (uri == null) {
            onComplete(null)
            return
        }

        val filename = UUID.randomUUID().toString() + ".jpg"
        val ref = storage.reference.child("company_images/$filename")

        val maxWidth = if (isIcon) 150 else 1024 // 아이콘: 150px, 큰 이미지: 1024px
        val maxHeight = if (isIcon) 150 else 768 // 아이콘: 150px, 큰 이미지: 768px

        resizeImage(uri, maxWidth, maxHeight, !isIcon) { resizedBytes ->
            if (resizedBytes == null) {
                onComplete(null)
                return@resizeImage
            }

            ref.putBytes(resizedBytes)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        onComplete(url.toString())
                    }
                }
                .addOnFailureListener {
                    onComplete(null)
                }
        }
    }

    private fun registerCompany() {
        val name = editCompanyName.text.toString().trim()
        val description = editCompanyDescription.text.toString().trim()
        val address = editCompanyAddress.text.toString().trim() // 주소 값 가져오기
        val isRecruiting = checkboxRecruiting.isChecked

        if (name.isEmpty() || description.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력하세요!", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageToStorage(largeImageUri, false) { largeUrl ->
            if (largeUrl == null) {
                Toast.makeText(requireContext(), "큰 이미지 업로드 실패!", Toast.LENGTH_SHORT).show()
                return@uploadImageToStorage
            }
            largeImageUrl = largeUrl

            uploadImageToStorage(iconImageUri, true) { iconUrl ->
                if (iconUrl == null) {
                    Toast.makeText(requireContext(), "아이콘 이미지 업로드 실패!", Toast.LENGTH_SHORT).show()
                    return@uploadImageToStorage
                }
                iconImageUrl = iconUrl

                val companyData = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "address" to address, // 주소 데이터 추가
                    "largeImageUrl" to largeImageUrl,
                    "iconImageUrl" to iconImageUrl,
                    "isRecruiting" to isRecruiting,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("companies")
                    .add(companyData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "회사 등록 성공!", Toast.LENGTH_SHORT).show()

                        // 홈 화면으로 이동
                        val navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
                        navController.navigate(R.id.nav_home)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "회사 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

}
