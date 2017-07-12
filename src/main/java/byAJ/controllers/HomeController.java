package byAJ.controllers;

import byAJ.configs.CloudinaryConfig;
import byAJ.models.Photo;
import byAJ.models.User;
import byAJ.repositories.PhotoRepository;
import byAJ.services.UserService;
import byAJ.validators.UserValidator;
import com.cloudinary.Singleton;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.cloudinary.Cloudinary;

import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {
	
	String photouser;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private UserService userService;
    
    @Autowired
    private PhotoRepository photoRepository;

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping(value="/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value="/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model){

        model.addAttribute("user", user);
        userValidator.validate(user, result);

        if (result.hasErrors()) {
            return "registration";
        } else {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Successfully Created");
        }
        return "index";
    }

    public UserValidator getUserValidator() {
        return userValidator;
    }

    public void setUserValidator(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    @GetMapping("/upload")
    public String uploadForm(Model model){
    model.addAttribute("pho", new Photo());
        return "upload";
    }

    @PostMapping("/upload")
    public String singleImageUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, Model model, @ModelAttribute Photo pho, Principal p){

        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("message","Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {
            Map uploadResult =  cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));

            model.addAttribute("message","You successfully uploaded '" + file.getOriginalFilename() + "'");
            model.addAttribute("imageurl", uploadResult.get("url"));
            String filename = uploadResult.get("public_id").toString() + "." + uploadResult.get("format").toString();
            model.addAttribute("sizedimageurl", cloudc.createUrl(filename,100,150, "fit"));
          //model.addAttribute("cropedimageurl", cloudc.createCropedUrl(filename,100,150, "fit","sepia"));
            photouser= p.getName();
            pho.setUsername(photouser);
            pho.setCreatedAt(new Date());
            pho.setImage("<img src='http://res.cloudinary.com/beticloud/image/upload/c_scale,h_100,w_100/"+filename+"'/>");
            photoRepository.save(pho);
        } catch (IOException e){
            e.printStackTrace();
            model.addAttribute("message", "Sorry I can't upload that!");
        }
      
        return "redirect:/photo";
    }
    @RequestMapping(value="/photo")
    public String photoview(Photo pho,  Model model, Principal p ){
    String phouser = p.getName();
    Iterable<Photo> photoList = photoRepository.findByUsername(phouser);
    model.addAttribute("newphoto", photoList);
    	return "view";
    }
    @RequestMapping(value="/delete/{id}")
    public String deletePhoto(@PathVariable(value ="id") long id,  Model model, Principal p ){
     photoRepository.delete(id);
       	return "view";
    }
    
   
    
    
}
