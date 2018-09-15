package com.pires.curso.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pires.curso.domain.Cliente;
import com.pires.curso.domain.ItemPedido;
import com.pires.curso.domain.PagamentoComBoleto;
import com.pires.curso.domain.Pedido;
import com.pires.curso.domain.enums.EstadoPagamento;
import com.pires.curso.repositories.ItemPedidoRepository;
import com.pires.curso.repositories.PagamentoRepository;
import com.pires.curso.repositories.PedidoRepository;
import com.pires.curso.security.UserSS;
import com.pires.curso.services.exceptions.AuthorizationException;
import com.pires.curso.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private EmailService emailService;
	
	
	public Pedido getById(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.getById(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pgto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.dataPagamentoBoleto(pgto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		
		for(ItemPedido ip: obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.getById(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);	
		}
		itemPedidoRepository.saveAll(obj.getItens());	
		emailService.sendOrderConfirmationHtmlEmail(obj);
		return obj;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String direction, String orderBy) {
		UserSS user = UserService.authenticated();
		
		if(user == null) {
			throw new AuthorizationException("acesso negado");
		}
		
		PageRequest pageRequest = PageRequest
				.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		
		Cliente cliente = clienteService.getById(user.getId());
		
		return repo.findByCliente(cliente, pageRequest);
	}
}
