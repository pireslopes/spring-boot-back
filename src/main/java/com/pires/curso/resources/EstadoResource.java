package com.pires.curso.resources;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pires.curso.domain.Cidade;
import com.pires.curso.domain.Estado;
import com.pires.curso.dto.CidadeDTO;
import com.pires.curso.dto.EstadoDTO;
import com.pires.curso.services.CidadeService;
import com.pires.curso.services.EstadoService;

@RestController
@RequestMapping(value="/estados")
public class EstadoResource {
	
	@Autowired
	private EstadoService service;
	
	@Autowired
	private CidadeService serviceCity;
	
	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<List<EstadoDTO>> findAll() {
		List<Estado> list = service.findAll();
		List<EstadoDTO> listDto = list.stream().map(dto -> new EstadoDTO(dto)).collect(Collectors.toList());
		return ResponseEntity.ok().body(listDto);
	}
	
	@RequestMapping(value="/{id}/cidades", method=RequestMethod.GET)
	public ResponseEntity<List<CidadeDTO>> findByEstado(@PathVariable Integer id) {
		List<Cidade> list = serviceCity.findByEstado(id);
		List<CidadeDTO> listDto = list.stream().map(obj -> new CidadeDTO(obj)).collect(Collectors.toList());
		return ResponseEntity.ok().body(listDto);
	}

}
