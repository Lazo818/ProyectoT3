package com.example.minimarket2.entity;

public class ProductoParaVender extends Producto{
	 private Float cantidad;

	    public ProductoParaVender(String nombre, String codigo, Float precio, Float stock, Integer id, Float cantidad) {
	        super(nombre, codigo, precio, stock, id);
	        this.cantidad = cantidad;
	    }

	    public ProductoParaVender(String nombre, String codigo, Float precio, Float stock, Float cantidad) {
	        super(nombre, codigo, precio,stock);
	        this.cantidad = cantidad;
	    }

	    public void aumentarCantidad() {
	        this.cantidad++;
	    }

	    public Float getCantidad() {
	        return cantidad;
	    }

	    public Float getTotal() {
	        return this.getPrecio() * this.cantidad;
	    }
}
