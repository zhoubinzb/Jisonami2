package org.jisonami.controller.blog;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jisonami.entity.Blog;
import org.jisonami.entity.BlogType;
import org.jisonami.service.BlogService;
import org.jisonami.service.BlogTypeService;
import org.jisonami.util.CollectionUtils;
import org.jisonami.vo.BlogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/blog/")
@SessionAttributes("username")
public class BlogController {

	@Autowired
	BlogService blogService;
	@Autowired
	BlogTypeService blogTypeService;
	@Autowired
	BlogBeanCopyFactory blogBeanCopyFactory;
	
	@RequestMapping("blogIndexForward.do")
	public String blogIndex(ModelMap model){
		try {
			List<Blog> blogs = blogService.query();
			List<BlogVO> blogVOs = new ArrayList<BlogVO>();
			CollectionUtils.copyList(blogs, blogVOs, BlogVO.class);
			model.put("blogs", blogVOs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/blog/blogIndex";
	}
	
	@RequestMapping("publish.do")
	public String publish(BlogVO blogVO, @ModelAttribute("username") String username, ModelMap model){
		try {
			Blog blog = new Blog();
			BeanUtils.copyProperties(blogVO, blog);
			blog.setBlogType(blogVO.getBlogTypeIds());
			blog.setAuthor(username);
			blog.setPublishTime(new Date());
			blogService.save(blog);
			
			// 查询该用户下的所有博客
			List<Blog> blogs = blogService.queryByAuthor(username);
			List<BlogVO> blogVOs = new ArrayList<BlogVO>();
			CollectionUtils.copyList(blogs, blogVOs, BlogVO.class, blogBeanCopyFactory.newBlogBeanCopy());
			model.put("blogs", blogVOs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 提示发布成功，3秒后跳转到blog页面
		return "/blog/blog";
	}
	
	@RequestMapping("edit.do")
	public String edit(BlogVO blogVO, String blogId, @ModelAttribute("username") String username, ModelMap model){
		Blog blog = new Blog();
		BeanUtils.copyProperties(blogVO, blog);
		blog.setId(blogId);
		blog.setBlogType(blogVO.getBlogTypeIds());
		blog.setEditTime(new Date());
		boolean result = false;
		try {
			result = blogService.edit(blog);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(result){
			// 查询该用户下的所有博客
			List<Blog> blogs;
			try {
				blogs = blogService.queryByAuthor(username);
				List<BlogVO> blogVOs = new ArrayList<BlogVO>();
				CollectionUtils.copyList(blogs, blogVOs, BlogVO.class, blogBeanCopyFactory.newBlogBeanCopy());
				model.put("blogs", blogVOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 返回博客列表
		}else{
			// 编辑不成功，返回错误提示
			
		}
		return "/blog/blog";
	}
	
	@RequestMapping("delete.do")
	public String delete(String blogId, @ModelAttribute("username") String username, ModelMap model){
		boolean result = false;
		try {
			result = blogService.delete(blogId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(result){
			// 查询该用户下的所有博客
			List<Blog> blogs;
			try {
				blogs = blogService.queryByAuthor(username);
				List<BlogVO> blogVOs = new ArrayList<BlogVO>();
				CollectionUtils.copyList(blogs, blogVOs, BlogVO.class, blogBeanCopyFactory.newBlogBeanCopy());
				model.put("blogs", blogVOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			// 编辑不成功，返回错误提示
			
		}
		return "/blog/blog";
	}
	
	@RequestMapping("blogForward.do")
	public String blogForward(String blogTypeId, @ModelAttribute("username") String username, ModelMap model){
		// 查询该用户下的所有博客
		List<Blog> blogs = null;
		try {
			if(blogTypeId!=null && !"".equals(blogTypeId)){
				blogs = blogService.queryByBlogType(blogTypeId);
			} else {
				blogs = blogService.queryByAuthor(username);
			}
			List<BlogVO> blogVOs = new ArrayList<BlogVO>();
			CollectionUtils.copyList(blogs, blogVOs, BlogVO.class, blogBeanCopyFactory.newBlogBeanCopy());
			model.put("blogs", blogVOs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/blog/blog";
	}
	
	@RequestMapping("ViewForward.do")
	public String viewForward(String blogId, ModelMap model){
		Blog blog = null;
		try {
			blog = blogService.queryById(blogId);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		if(blog!=null){
			model.put("blog", blog);
		}
		
		String blogTypeIds = blog.getBlogType();
		String blogTypes = "";
		List<String> blogTypeIdList = Arrays.asList(blogTypeIds.split(","));
		for(int i=0;i<blogTypeIdList.size();i++){
			String blogTypeId = blogTypeIdList.get(i);
			BlogType blogType = null;
			try {
				blogType = blogTypeService.queryById(blogTypeId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(i < blogTypeIdList.size()-1){
				blogTypes = blogTypes + blogType.getName() + ",";
			} else {
				blogTypes = blogTypes + blogType.getName();
			}
		}
		if(blogTypeIds!=null && !"".equals(blogTypeIds)){
			model.put("blogTypeIds", blogTypeIds);
		}
		if(blogTypes!=null && !"".equals(blogTypes)){
			model.put("blogTypes", blogTypes);
		}
		return "/blog/view";
	}
	
	@RequestMapping("EditForward.do")
	public String editForward(String blogId, @ModelAttribute("username") String username, ModelMap model){
		Blog blog = null;
		try {
			blog = blogService.queryById(blogId);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		if(blog!=null){
			model.put("blog", blog);
		}
		
		String blogTypeIds = blog.getBlogType();
		String blogTypes = "";
		List<String> blogTypeIdList = Arrays.asList(blogTypeIds.split(","));
		for(int i=0;i<blogTypeIdList.size();i++){
			String blogTypeId = blogTypeIdList.get(i);
			BlogType blogType = null;
			try {
				blogType = blogTypeService.queryById(blogTypeId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(i < blogTypeIdList.size()-1){
				blogTypes = blogTypes + blogType.getName() + ",";
			} else {
				blogTypes = blogTypes + blogType.getName();
			}
		}
		if(blogTypeIds!=null && !"".equals(blogTypeIds)){
			model.put("blogTypeIds", blogTypeIds);
		}
		if(blogTypes!=null && !"".equals(blogTypes)){
			model.put("blogTypes", blogTypes);
		}
		
		List<BlogType> blogTypeList = null;
		try {
			blogTypeList = blogTypeService.queryByAuthor(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		model.put("blogTypeList", blogTypeList);
		
		return "/blog/edit";
	}
	
	@RequestMapping("publishForward.do")
	public String publishForward(@ModelAttribute("username") String username, ModelMap model){
		List<BlogType> blogTypeList = null;
		try {
			blogTypeList = blogTypeService.queryByAuthor(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		model.put("blogTypeList", blogTypeList);
		return "/blog/publish";
	}
	
}
