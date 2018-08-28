package com.pires.curso.services;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.pires.curso.domain.PagamentoComBoleto;

@Service
public class BoletoService {
	public void dataPagamentoBoleto(PagamentoComBoleto obj, Date instanteDoPedido) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(instanteDoPedido);
		cal.add(Calendar.DAY_OF_MONTH, 7);
		obj.setDataVencimento(cal.getTime());
	}
}
