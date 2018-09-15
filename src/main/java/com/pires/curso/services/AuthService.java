package com.pires.curso.services;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.pires.curso.domain.Cliente;
import com.pires.curso.repositories.ClienteRepository;
import com.pires.curso.services.exceptions.ObjectNotFoundException;

@Service
public class AuthService {
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	private BCryptPasswordEncoder bpe;
	
	@Autowired
	private EmailService emailService;
	
	private Random rand = new Random();
	
	public void sendNewPasswordEmail(String email) {
		
		Cliente cliente = clienteRepository.findByEmail(email);
		if(cliente == null) {
			throw new ObjectNotFoundException("E-mail não encontrado");
		}
		String newPass = GeneratePassword();
		cliente.setSenha(bpe.encode(newPass));
		
		clienteRepository.save(cliente);
		emailService.sendNewPasswordEmail(cliente, newPass);
	}

	private String GeneratePassword() {
		char [] vet = new char[10];
		for(int i = 0; i<10; i++) {
			vet[i] = randomChar();
		}
		return new String(vet);
	}

	private char randomChar() {
		int opt = rand.nextInt(3);
		
		if(opt == 0) { //ascii (10 48) (26,65) (26, 97)
			return (char) (rand.nextInt(10) + 48);//Gera um digito 
		} 
		else if(opt == 1) {
			return (char) (rand.nextInt(26) + 65); //Gera uma letra maiuscula
		}
		else {
			return (char) (rand.nextInt(26) + 97); //Gera uma letra minuscula 
		}		
	}
}
