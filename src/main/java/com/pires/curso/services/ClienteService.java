package com.pires.curso.services;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pires.curso.domain.Cidade;
import com.pires.curso.domain.Cliente;
import com.pires.curso.domain.Endereco;
import com.pires.curso.domain.enums.Perfil;
import com.pires.curso.domain.enums.TipoCliente;
import com.pires.curso.dto.ClienteDTO;
import com.pires.curso.dto.ClienteNewDTO;
import com.pires.curso.repositories.ClienteRepository;
import com.pires.curso.repositories.EnderecoRepository;
import com.pires.curso.security.UserSS;
import com.pires.curso.services.exceptions.AuthorizationException;
import com.pires.curso.services.exceptions.DataIntegrityException;
import com.pires.curso.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Value("${img.prefix.client.profile}")
	private String imgPrefix;

	@Value("${img.profile.size}")
	private int size;

	@Autowired
	private BCryptPasswordEncoder pe;

	@Autowired
	private ClienteRepository repo;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private ImageService imageService;

	public Cliente getById(Integer id) {

		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}

		Optional<Cliente> obj = repo.findById(id);

		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente insert(Cliente entity) {
		entity.setId(null);
		entity = repo.save(entity);
		enderecoRepository.saveAll(entity.getEnderecos());
		return entity;
	}

	public Cliente update(Cliente entity) {
		Cliente objCli = getById(entity.getId());
		updateData(objCli, entity);
		return repo.save(objCli);
	}

	public void remove(Integer id) {
		getById(id);
		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possivel excluir um cliente que possui pedidos");
		}
	}

	public List<Cliente> getAll() {
		return repo.findAll();
	}

	public Cliente findByEmail(String email) {
		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) || !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}

		Cliente obj = repo.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException(
					"Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + this.getClass());
		}
		return obj;
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String direction, String orderBy) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	public Cliente fromDto(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}

	private void updateData(Cliente objCli, Cliente entity) {
		objCli.setNome(entity.getNome());
		objCli.setEmail(entity.getEmail());
	}

	public Cliente fromDto(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(),
				TipoCliente.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(),
				objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone());
		if (objDto.getTelefone1() != null) {
			cli.getTelefones().add(objDto.getTelefone1());
		}
		if (objDto.getTelefone2() != null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		return cli;
	}

	public URI uploadProfilePicture(MultipartFile multipartFile) {
		UserSS user = UserService.authenticated();
		if (user == null) {
			throw new AuthorizationException("Acesso negado");
		}

		BufferedImage jpgImage = imageService.getJpgImageFromFile(multipartFile);
		jpgImage = imageService.cropSquare(jpgImage);
		jpgImage = imageService.resize(jpgImage, size);

		String fileName = imgPrefix + user.getId() + "jpg";

		return s3Service.uploadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");

	}
}
