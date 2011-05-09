package org.blim.whist.player;

import java.io.IOException;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@SessionAttributes(types = HumanPlayer.class)
@RequestMapping("/players/register")
public interface NewForm {

	public abstract void setAllowedFields(WebDataBinder dataBinder);

	@RequestMapping(method = RequestMethod.GET)
	public abstract ModelAndView setupForm() throws IOException;

	@RequestMapping(method = RequestMethod.PUT)
	public abstract ModelAndView processSubmit(
			@ModelAttribute("player") HumanPlayer humanPlayer, BindingResult result,
			SessionStatus status);

}