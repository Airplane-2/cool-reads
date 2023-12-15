package fi.haagahelia.coolreads.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fi.haagahelia.coolreads.dto.RecommendationDto;
import fi.haagahelia.coolreads.model.AppUser;
import fi.haagahelia.coolreads.model.Recommendation;
import fi.haagahelia.coolreads.repository.ReadingRecommendationRepository;
import jakarta.validation.Valid;
import fi.haagahelia.coolreads.repository.AppUserRepository;
import fi.haagahelia.coolreads.repository.CategoryRepository;

//import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReadingRecommendationController {
	@Autowired
	private ReadingRecommendationRepository readingRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private AppUserRepository userRepository;

	private static final Logger log = LoggerFactory.getLogger(ReadingRecommendationController.class);

	// Add new recommendation
	@GetMapping("/add")
	public String addRecommendation(Model model) {
		model.addAttribute("recommendation", new RecommendationDto());
		model.addAttribute("categories", categoryRepository.findAll());
		log.info("form");
		return "addrecommendation";
	}

	// Save new recommendation
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String saveRecommendation(@Valid @ModelAttribute("recommendation") RecommendationDto recommendation, BindingResult bindingResult,
				@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if(bindingResult.hasErrors()) {
			model.addAttribute("recommendation", recommendation);
			return "addrecommendation";
		}
		
		AppUser user = userRepository.findByUsername(userDetails.getUsername());
		
		if (user == null) {
			return "redirect:/";
		}
		Recommendation newRecommendation = new Recommendation(recommendation.getTitle(), recommendation.getLink(), recommendation.getDescription(),
				recommendation.getCategory(), user);
		readingRepository.save(newRecommendation);
		return "redirect:/";
	}

	@GetMapping("/")
	public String listRecommendation(Model model) {
		List<Recommendation> recommendations = readingRepository.findAll();
		model.addAttribute("recommendations", recommendations);
		return "recommendationlist";
	}

	@GetMapping("/createdDate")
	public String listRecommendationByDateCreatedDesc(Model model) {
		List<Recommendation> recommendations = readingRepository.findAllByOrderByCreationDateDesc();
		model.addAttribute("recommendations", recommendations);
		return "recommendationlist";
	}

	@GetMapping("/edit/{id}")
	public String editReadingRecommendation(@PathVariable("id") Long id, Model model) {
		model.addAttribute("categories", categoryRepository.findAll());
		Recommendation recommendation = readingRepository.findById(id).orElse(null);

		if (recommendation != null) {
			model.addAttribute("recommendation", recommendation);
			return "editrecommendation";
		} else {
			return "redirect:/";
		}
	}

	@PostMapping("/saveEditedReadingRecommendation")
	public String saveEditedBook(@ModelAttribute Recommendation recommendationForm) {
		readingRepository.save(recommendationForm);
		return "redirect:/";
	}

	@PostMapping("/delete/{id}")
	public String deleteRecommendation(@PathVariable("id") Long id, Model model) {
		readingRepository.deleteById(id);
		return "redirect:/";
	}
}
