package com.eve.notas.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.eve.notas.data.model.Student
import java.io.File
import android.graphics.Paint
import android.os.Environment
import java.io.FileOutputStream

fun List<Student>.generatePdf(context: Context, fileName: String = "estudiantes.pdf"): File {
    val pdfDocument = PdfDocument()
    val paint = Paint().apply { textSize = 14f }
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    var y = 80f

    // 🔹 Título
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("Lista de Estudiantes", 200f, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false

    // 🔹 Encabezados
    val startXName = 50f
    val startXAverage = 350f
    canvas.drawText("Nombre", startXName, y, paint)
    canvas.drawText("Promedio", startXAverage, y, paint)
    y += 20f

    // 🔹 Línea debajo de encabezados
    canvas.drawLine(40f, y, 550f, y, paint)
    y += 20f

    // 🔹 Filas con colores alternos
    forEachIndexed { index, student ->
        val rowHeight = 25f
        val left = 40f
        val right = 550f
        val top = y
        val bottom = y + rowHeight

        val bgPaint = Paint().apply {
            color = if (index % 2 == 0) android.graphics.Color.LTGRAY else android.graphics.Color.WHITE
        }
        canvas.drawRect(left, top, right, bottom, bgPaint)

        paint.color = android.graphics.Color.BLACK
        canvas.drawText(student.name, startXName, y + 18f, paint)
        canvas.drawText(String.format("%.2f", student.average), startXAverage, y + 18f, paint)

        canvas.drawLine(left, bottom, right, bottom, paint)
        y += rowHeight
    }

    pdfDocument.finishPage(page)

    // 🔹 Guardar en carpeta pública Descargas
    val file = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        fileName
    )
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    return file
}