package com.example.minimarket2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.minimarket2.entity.Producto;
import com.example.minimarket2.entity.ProductoParaVender;
import com.example.minimarket2.entity.ProductoVendido;
import com.example.minimarket2.entity.Usuario;
import com.example.minimarket2.entity.Venta;
import com.example.minimarket2.repository.ProductoRepository;
import com.example.minimarket2.service.ProductoService;
import com.example.minimarket2.service.ProductoVendidoService;
import com.example.minimarket2.service.UsuarioService;
import com.example.minimarket2.service.VentaService;
import com.example.minimarket2.service.impl.NotificationService;

import ch.qos.logback.classic.Logger;

@Controller
@RequestMapping(value = "/vender")
public class VenderController {
	@Autowired
	private ProductoService productoService;
	@Autowired
	private VentaService ventaService;
	@Autowired
	private ProductoVendidoService productoVendidoService;
	@Autowired
	private UsuarioService usuarioService;

	 @Autowired
	    private ProductoRepository productoRepository;
	 
	 @Autowired
		private NotificationService notificationService;
	 
	 private Logger logger = (Logger) LoggerFactory.getLogger(ProductoController.class);
	 
	@PostMapping(value = "/quitar/{id}")
	public String quitarDelCarrito(@PathVariable int id, HttpServletRequest request) {
		ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
		if (carrito != null && carrito.size() > 0 && carrito.get(id) != null) {
			carrito.remove(id);
			this.guardarCarrito(carrito, request);
		}
		return "redirect:/vender";
	}

	private void limpiarCarrito(HttpServletRequest request) {
		this.guardarCarrito(new ArrayList<>(), request);
	}
	
	@GetMapping(value = "/limpiar")
	public String cancelarVenta(HttpServletRequest request, RedirectAttributes redirectAttrs) {
		this.limpiarCarrito(request);
		redirectAttrs.addFlashAttribute("mensaje", "Venta cancelada").addFlashAttribute("clase", "info");
		return "redirect:/vender";
	}

	@PostMapping(value = "/terminar")
	public String terminarVenta(HttpServletRequest request, RedirectAttributes redirectAttrs,SessionStatus status ,Model model) throws Exception {
		ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		// Si no hay carrito o está vacío, regresamos inmediatamente
		if (carrito == null || carrito.size() <= 0) {
			return "redirect:/vender/";
		}try {
			Optional<Usuario> usuario = usuarioService.findByUsername(username);
			if(usuario.isPresent()) {
				model.addAttribute("usuario", usuario.get());
			}
			Venta v = ventaService.save(new Venta());
			v.setUsuario(usuario.get());
			status.setComplete();
			// Recorrer el carrito
			for (ProductoParaVender productoParaVender : carrito) {
				// Obtener el producto fresco desde la base de datos
				Producto p = productoService.findById(productoParaVender.getId()).orElse(null);
				if (p == null)
					continue; // Si es nulo o no existe, ignoramos el siguiente código con continue
				// Le restamos existencia
				p.restarStock(productoParaVender.getCantidad());
				// Lo guardamos con la existencia ya restada
				productoService.save(p);
				// Creamos un nuevo producto que será el que se guarda junto con la venta
				ProductoVendido productoVendido = new ProductoVendido(productoParaVender.getCantidad(),
						productoParaVender.getPrecio(), productoParaVender.getNombre(),productoParaVender.getCodigo(), v);
				// Y lo guardamos
				productoVendidoService.save(productoVendido);
			}

			// Al final limpiamos el carrito
			this.limpiarCarrito(request);
			// e indicamos una venta exitosa
			redirectAttrs.addFlashAttribute("mensaje", "Venta realizada correctamente").addFlashAttribute("clase",
					"success");
			//Se envia el consolidado de pago al correo
		}catch (Exception e) {
			// TODO: handle exception
		}
		return "redirect:/vender";
	}

	@GetMapping()
	public String interfazVender(Model model, HttpServletRequest request) {
		model.addAttribute("producto", new Producto());
		float total = 0;
		ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
		for (ProductoParaVender p : carrito)
			total += p.getTotal();
		model.addAttribute("total", total);
		try {
			List<Producto> productos = productoService.findAll();
			model.addAttribute("productos", productos);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return "vender/inicio";
	}

	private ArrayList<ProductoParaVender> obtenerCarrito(HttpServletRequest request) {
		ArrayList<ProductoParaVender> carrito = (ArrayList<ProductoParaVender>) request.getSession().getAttribute("carrito");
		if (carrito == null) {
			carrito = new ArrayList<>();
		}
		return carrito;
	}

	private void guardarCarrito(ArrayList<ProductoParaVender> carrito, HttpServletRequest request) {
		request.getSession().setAttribute("carrito", carrito);
	}
	
	@PostMapping(value = "/agregar")
    public String agregarAlCarrito(@ModelAttribute ("producto") Producto producto, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        Producto productoBuscadoPorCodigo = productoRepository.findFirstByCodigo(producto.getCodigo());
        if (productoBuscadoPorCodigo == null) {
        	redirectAttrs
            .addFlashAttribute("mensaje", "El producto con el código " + producto.getCodigo() + " no existe")
            .addFlashAttribute("clase", "warning");
            return "redirect:/vender";
        }
        if (productoBuscadoPorCodigo.sinStock()) {
        	redirectAttrs
            .addFlashAttribute("mensaje", "El producto está agotado")
            .addFlashAttribute("clase", "warning");
            return "redirect:/vender";
        }
        boolean encontrado = false;
        for (ProductoParaVender productoParaVenderActual : carrito) {
            if (productoParaVenderActual.getCodigo().equals(productoBuscadoPorCodigo.getCodigo())) {
                productoParaVenderActual.aumentarCantidad();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            carrito.add(new ProductoParaVender(productoBuscadoPorCodigo.getNombre(), productoBuscadoPorCodigo.getCodigo(), productoBuscadoPorCodigo.getPrecio(), productoBuscadoPorCodigo.getStock(), productoBuscadoPorCodigo.getId(), 1f));
        }
        this.guardarCarrito(carrito, request);
        return "redirect:/vender";
    }
}
