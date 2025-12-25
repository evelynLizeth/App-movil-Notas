package com.eve.notas.util


import android.content.Context
import android.graphics.pdf.PdfDocument
import com.eve.notas.data.model.Student
import java.io.File
import android.graphics.Paint
import java.io.FileOutputStream




fun List<Student>.generatePdf(context: Context, fileName: String = "estudiantes.pdf"): File {
    val pdfDocument = PdfDocument()
    val paint = Paint().apply { textSize = 14f }
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    var y = 50
    canvas.drawText("Lista de Estudiantes", 200f, y.toFloat(), paint.apply {
        textSize = 18f
        isFakeBoldText = true
    })
    y += 40
    paint.textSize = 14f
    paint.isFakeBoldText = false

    canvas.drawText("Nombre", 50f, y.toFloat(), paint)
    canvas.drawText("Promedio", 350f, y.toFloat(), paint)
    y += 30

    forEach {
        canvas.drawText(it.name, 50f, y.toFloat(), paint)
        canvas.drawText(String.format("%.2f", it.average), 350f, y.toFloat(), paint)
        y += 25
    }

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(null), fileName)
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()
    return file
}