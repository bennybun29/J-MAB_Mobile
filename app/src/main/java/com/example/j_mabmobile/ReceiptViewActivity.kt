package com.example.j_mabmobile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.j_mabmobile.databinding.ActivityReceiptViewBinding
import com.example.j_mabmobile.model.Receipt
import com.example.j_mabmobile.viewmodels.ReceiptViewModel
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ReceiptViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiptViewBinding
    private lateinit var receiptItemAdapter: ReceiptItemAdapter
    private lateinit var viewModel: ReceiptViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)


        viewModel = ViewModelProvider(this)[ReceiptViewModel::class.java]
        val orderId = intent.getIntExtra("ORDER_ID", -1)

        // Receive the receipt data
        val receipt = intent.getParcelableExtra<Receipt>("RECEIPT_DATA")
        receipt?.let { displayReceiptDetails(it) }

        if (orderId != -1) {
            // Show loading indicator
            binding.progressBar.visibility = View.VISIBLE

            // Observe receipt data
            viewModel.receiptLiveData.observe(this) { result ->
                // Hide loading indicator
                binding.progressBar.visibility = View.GONE

                result.onSuccess { receiptResponse ->
                    // Check if receipt is successful and not null
                    if (receiptResponse.success && receiptResponse.receipt != null) {
                        displayReceiptDetails(receiptResponse.receipt)

                        // Setup PDF Save Button
                        binding.btnSavePdf.setOnClickListener {
                            generatePdf(receiptResponse.receipt)
                        }

                        binding.backBtn.setOnClickListener{
                            onBackPressed()
                        }
                    } else {
                        // Handle case where receipt retrieval was not successful
                        Toast.makeText(
                            this,
                            "Could not retrieve receipt",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.onFailure { exception ->
                    // Handle error
                    Toast.makeText(
                        this,
                        "Error: ${exception.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Trigger API call
            viewModel.fetchReceipt(orderId)
        } else {
            Toast.makeText(this, "Invalid Order ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    //Comment out this function pag hindi maganda gamitin HAHAHA
    private fun generateReceiptQRCode(receipt: Receipt): Bitmap {
        // Create a compact JSON representation of key receipt details
        val qrCodeContent = JSONObject().apply {
            put("order_reference", receipt.order_reference)
            put("total_amount", receipt.total_amount)
            put("payment_status", receipt.payment_status)

            // Add bill to details
            val billToObj = JSONObject().apply {
                put("name", receipt.bill_to.name)
                put("address", receipt.bill_to.address)
            }
            put("bill_to", billToObj)

            // Add item summary
            val itemsArray = JSONArray()
            receipt.items.forEach { item ->
                val itemObj = JSONObject().apply {
                    put("model", item.model)
                    put("variant", item.variant)
                    put("quantity", item.quantity)
                    put("amount", item.amount)
                }
                itemsArray.put(itemObj)
            }
            put("items", itemsArray)
        }.toString()

        // Generate QR Code
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(
                qrCodeContent,
                com.google.zxing.BarcodeFormat.QR_CODE,
                512,
                512
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                    )
                }
            }

            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            // Return a placeholder or handle error as needed
            return Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        }
    }

    private fun displayReceiptDetails(receipt: Receipt) {
        binding.apply {
            tvOrderReference.text = "Order Ref: ${receipt.order_reference}"

            // Bill To Details
            tvBillToName.text = receipt.bill_to.name
            tvBillToAddress.text = receipt.bill_to.address

            // Ship To Details
            tvShipToName.text = receipt.ship_to.name
            tvShipToAddress.text = receipt.ship_to.address

            // Payment Details
            tvPaymentMethod.text = "Payment Method: ${receipt.payment_method}"
            tvPaymentStatus.text = "Payment Status: ${receipt.payment_status}"

            // Setup RecyclerView for Items
            receiptItemAdapter = ReceiptItemAdapter(receipt.items)
            rvReceiptItems.layoutManager = LinearLayoutManager(this@ReceiptViewActivity)
            rvReceiptItems.adapter = receiptItemAdapter

            // Total Amount
            tvTotalAmount.text = "Total: ₱${receipt.total_amount}"


            //Comment this out pag hindi maganda gamitin HAHAHA
            val qrCodeBitmap = generateReceiptQRCode(receipt)
            binding.ivReceiptQrCode.setImageBitmap(qrCodeBitmap)
        }
    }

    private fun generatePdf(receipt: Receipt?) {
        receipt ?: return

        val pdfFile = File(getExternalFilesDir(null), "receipt_${receipt.order_reference}.pdf")

        try {
            // Create a bitmap of the entire LinearLayout
            val scrollView = binding.root.findViewById<ScrollView>(R.id.scrollView)
            val linearLayout = scrollView.getChildAt(0) as LinearLayout

            // Measure and layout the view
            linearLayout.measure(
                View.MeasureSpec.makeMeasureSpec(linearLayout.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            linearLayout.layout(0, 0, linearLayout.measuredWidth, linearLayout.measuredHeight)

            // Create bitmap from the view
            val bitmap = Bitmap.createBitmap(
                linearLayout.width,
                linearLayout.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            linearLayout.draw(canvas)

            // Create PDF document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                1
            ).create()
            val page = pdfDocument.startPage(pageInfo)

            // Draw bitmap to PDF page
            val pageCanvas = page.canvas
            pageCanvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // Write PDF to file
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            // Share or open PDF
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)

            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}