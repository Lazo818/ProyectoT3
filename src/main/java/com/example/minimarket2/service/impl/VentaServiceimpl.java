package com.example.minimarket2.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.minimarket2.entity.Venta;
import com.example.minimarket2.repository.VentaRepository;
import com.example.minimarket2.service.VentaService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class VentaServiceimpl implements VentaService{

	@Autowired
	VentaRepository ventaRepository;
	
	@Transactional(readOnly = true)
	@Override
	public List<Venta> findAll() throws Exception {
		return ventaRepository.findAll();
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<Venta> findById(Integer id) throws Exception {
		return ventaRepository.findById(id);
	}

	@Transactional
	@Override
	public Venta save(Venta entity) throws Exception {
		return ventaRepository.save(entity);
	}

	@Transactional
	@Override
	public Venta update(Venta entity) throws Exception {
		return ventaRepository.save(entity);
	}

	@Transactional
	@Override
	public void deleteById(Integer id) throws Exception {
		ventaRepository.deleteById(id);
	}

	@Transactional
	@Override
	public void deleteAll() throws Exception {
		ventaRepository.deleteAll();
	}

	@Override
	public boolean createPdf(List<Venta> ventas, ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		Document document = new Document(PageSize.A4,15,15,45,30);
		try {
			String filePath = context.getRealPath("/resources/reports");
			File file = new File (filePath);
			boolean exists= new File(filePath).exists();
			if(!exists) {
				new File (filePath).mkdirs();
			}
			
			PdfWriter writer= PdfWriter.getInstance(document, new FileOutputStream(file+"/"+"ventas"+".pdf"));
			document.open();
			Font mainFont = FontFactory.getFont("Arial",10,BaseColor.BLACK);
			
			Paragraph paragraph = new Paragraph("Ventas",mainFont);
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.setIndentationLeft(50);
			paragraph.setIndentationRight(50);
			paragraph.setSpacingAfter(10);
			document.add(paragraph);
			
			PdfPTable table = new PdfPTable(3);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);
			table.setSpacingAfter(10);
			
			Font tableHeader = FontFactory.getFont("Arial",10,BaseColor.BLACK);
			Font tableBody = FontFactory.getFont("Arial",9,BaseColor.BLACK);
			
			float[] columnWidths = {2f,2f,2f};
			table.setWidths(columnWidths);
			
			PdfPCell id = new PdfPCell(new Paragraph("Id",tableHeader));
			id.setBorderColor(BaseColor.BLACK);
			id.setPaddingLeft(10);
			id.setHorizontalAlignment(Element.ALIGN_CENTER);
			id.setVerticalAlignment(Element.ALIGN_CENTER);
			id.setBackgroundColor(BaseColor.GRAY);
			id.setExtraParagraphSpace(5f);
			table.addCell(id);
			
			PdfPCell fechaYHora = new PdfPCell(new Paragraph("fechaYHora",tableHeader));
			fechaYHora.setBorderColor(BaseColor.BLACK);
			fechaYHora.setPaddingLeft(10);
			fechaYHora.setHorizontalAlignment(Element.ALIGN_CENTER);
			fechaYHora.setVerticalAlignment(Element.ALIGN_CENTER);
			fechaYHora.setBackgroundColor(BaseColor.GRAY);
			fechaYHora.setExtraParagraphSpace(5f);
			table.addCell(fechaYHora); 
			
			PdfPCell total = new PdfPCell(new Paragraph("Total",tableHeader));
			total.setBorderColor(BaseColor.BLACK);
			total.setPaddingLeft(10);
			total.setHorizontalAlignment(Element.ALIGN_CENTER);
			total.setVerticalAlignment(Element.ALIGN_CENTER);
			total.setBackgroundColor(BaseColor.GRAY);
			total.setExtraParagraphSpace(5f);
			table.addCell(total); 
			
			for(Venta venta : ventas) {
				PdfPCell idValue = new PdfPCell(new Paragraph(venta.getId()+"",tableBody));
				idValue.setBorderColor(BaseColor.BLACK);
				idValue.setPaddingLeft(10);
				idValue.setHorizontalAlignment(Element.ALIGN_CENTER);
				idValue.setVerticalAlignment(Element.ALIGN_CENTER);
				idValue.setBackgroundColor(BaseColor.WHITE);
				idValue.setExtraParagraphSpace(5f);
				table.addCell(idValue);
				
				PdfPCell fechaYHoraValue = new PdfPCell(new Paragraph(venta.getFechaYHora(),tableBody));
				fechaYHoraValue.setBorderColor(BaseColor.BLACK);
				fechaYHoraValue.setPaddingLeft(10);
				fechaYHoraValue.setHorizontalAlignment(Element.ALIGN_CENTER);
				fechaYHoraValue.setVerticalAlignment(Element.ALIGN_CENTER);
				fechaYHoraValue.setBackgroundColor(BaseColor.WHITE);
				fechaYHoraValue.setExtraParagraphSpace(5f);
				table.addCell(fechaYHoraValue);
				
				PdfPCell totalValue = new PdfPCell(new Paragraph(venta.getTotal()+"",tableBody));
				totalValue.setBorderColor(BaseColor.BLACK);
				totalValue.setPaddingLeft(10);
				totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
				totalValue.setVerticalAlignment(Element.ALIGN_CENTER);
				totalValue.setBackgroundColor(BaseColor.WHITE);
				totalValue.setExtraParagraphSpace(5f);
				table.addCell(totalValue); 
				
			}
			document.add(table);
			document.close();
			return true;
			
		} catch (Exception e) {
			System.out.println("Error");
			return false;
		}
	}

}
