import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.example.godparttimejob.ui.companydetails.Review
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(
    private val reviews: List<Review>,
    private val db: FirebaseFirestore,
    private val companyId: String
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textProfileName: TextView = itemView.findViewById(R.id.textProfileName)
        private val textWorkIntensity: TextView = itemView.findViewById(R.id.textWorkIntensity)
        private val textSalary: TextView = itemView.findViewById(R.id.textSalary)
        private val textEnvironment: TextView = itemView.findViewById(R.id.textEnvironment)
        private val textComment: TextView = itemView.findViewById(R.id.textComment)
        private val buttonLike: ImageButton = itemView.findViewById(R.id.buttonLike)
        private val textLikeCount: TextView = itemView.findViewById(R.id.textLikeCount)
        private val buttonReport: Button = itemView.findViewById(R.id.buttonReport)

        fun bind(review: Review) {
            textProfileName.text = review.userName
            textWorkIntensity.text = "근무 강도: ★${review.workIntensity}"
            textSalary.text = "급여: ★${review.salary}"
            textEnvironment.text = "환경: ★${review.environment}"
            textComment.text = review.comment
            textLikeCount.text = review.likes.toString()

            buttonLike.setOnClickListener {
                handleLike(review)
            }

            buttonReport.setOnClickListener {
                showReportDialog(review)
            }
        }

        private fun handleLike(review: Review) {
            val reviewRef = db.collection("companies").document(companyId)
                .collection("reviews").document(review.id!!)
            reviewRef.update("likes", review.likes + 1)
                .addOnSuccessListener {
                    Toast.makeText(itemView.context, "좋아요를 눌렀습니다!", Toast.LENGTH_SHORT).show()
                    review.likes += 1
                    textLikeCount.text = review.likes.toString()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "좋아요 실패!", Toast.LENGTH_SHORT).show()
                }
        }

        private fun showReportDialog(review: Review) {
            val context = itemView.context
            val editText = EditText(context).apply {
                hint = "신고 사유를 입력하세요"
            }

            AlertDialog.Builder(context)
                .setTitle("리뷰 신고")
                .setView(editText)
                .setPositiveButton("확인") { _, _ ->
                    val reason = editText.text.toString().trim()
                    if (reason.isNotEmpty()) {
                        reportReview(review, reason)
                    } else {
                        Toast.makeText(context, "신고 사유를 입력하세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        private fun reportReview(review: Review, reason: String) {
            val reportData = hashMapOf(
                "reviewId" to review.id,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("reported_reviews").add(reportData)
                .addOnSuccessListener {
                    Toast.makeText(itemView.context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "신고 접수 실패!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
