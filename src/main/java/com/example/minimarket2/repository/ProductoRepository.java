package com.example.minimarket2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.minimarket2.entity.Producto;


@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
	Producto findFirstByCodigo(String codigo);
	
	List<Producto> findByStock(Float stock);
	@Query(value="select * from producto where stock<4", nativeQuery=true)
	List<Producto> listaproducto();
}
