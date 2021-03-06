package br.com.doandersonferreira.forum.controller;

import java.net.URI;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.doandersonferreira.forum.controller.dto.DetalhesDoTopicoDto;
import br.com.doandersonferreira.forum.controller.dto.TopicoDto;
import br.com.doandersonferreira.forum.controller.form.AtualizacaoTopicoForm;
import br.com.doandersonferreira.forum.controller.form.TopicoForm;
import br.com.doandersonferreira.forum.model.Topico;
import br.com.doandersonferreira.forum.repository.CursoRepository;
import br.com.doandersonferreira.forum.repository.TopicoRepository;

@RestController // Indica que a classe é um controller Spring e retorna os objetos como JSON
@RequestMapping("/topicos") // Indica o contexto mapeado pelo controller
public class TopicosController {

	// Injeção de dependêcia
	@Autowired
	private TopicoRepository topicoRepository;

	@Autowired
	private CursoRepository cursoRepository;

	
	@GetMapping // Mapeado o método GET para o path '/'
	@Cacheable(value="listaDeTopicos")
	public Page<TopicoDto> lista(@RequestParam(required = false) String nomeCurso, 
			// Definicao do Pageable diretamente no metodo
			// Atributos de paginacao e ordenacao opcionais e permite definir o default
			// ex. da chamada '/topicos?page=0&size=10&sort=id,desc'
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.DESC) Pageable paginacao){		
			
		
		if(nomeCurso == null) {
			// Obtem lista de tópico do banco de dados
			Page<Topico> topicos = topicoRepository.findAll(paginacao);
			return TopicoDto.converter(topicos);	
		}else {
			Page<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
			return TopicoDto.converter(topicos);
		}
		
	}
	
	@PostMapping // Mapeado o método POST para o path '/'
	@Transactional // Metodos de escrita (write, update, delete) devem ser anotados para indicar ao Spring que commite ao final da transacao
	@CacheEvict(value="listaDeTopicos",allEntries = true) // Limpa cache de listaDeTopicos devido a uma escrita na base de dados
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) {

		Topico topico = form.converter(cursoRepository);
		topicoRepository.save(topico);
		
		// Implementando o retorno do código HTTP (201 - Created) e o recurso
		// no corpo
		URI uri = uriBuilder.path("topicos/{id}").buildAndExpand(topico.getId()).toUri();
		return ResponseEntity.created(uri).body(new TopicoDto(topico));
	}
	
	@GetMapping("/{id}") // Mapeado o método POST para o path '/{id}'
	public ResponseEntity<DetalhesDoTopicoDto> detalhar(@PathVariable Long id) {
		Optional<Topico> topico = topicoRepository.findById(id);
		
		if(topico.isPresent()) {
			return ResponseEntity.ok(new DetalhesDoTopicoDto(topico.get()));
		}
		
		return ResponseEntity.notFound().build();
	
	}
	
	@PutMapping("/{id}") // Mapeado o método PUT para o path '/{id}'
	@Transactional
	@CacheEvict(value="listaDeTopicos",allEntries = true) // Limpa cache de listaDeTopicos devido a uma escrita na base de dados
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form) {

		Optional<Topico> optional = topicoRepository.findById(id);
		
		if(optional.isPresent()) {
			Topico topico = form.atualizar(id, topicoRepository);
			return ResponseEntity.ok(new TopicoDto(topico));
		}
		
		return ResponseEntity.notFound().build();
		
		
	}
	
	@DeleteMapping("/{id}")
	@Transactional
	@CacheEvict(value="listaDeTopicos",allEntries = true) // Limpa cache de listaDeTopicos devido a uma escrita na base de dados
	public ResponseEntity<?> remover(@PathVariable Long id){
		
		Optional<Topico> optional = topicoRepository.findById(id);
		
		if(optional.isPresent()) {
			topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}
		
		return ResponseEntity.notFound().build();
		
	}
	
}
