
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 会议信息
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/huiyi")
public class HuiyiController {
    private static final Logger logger = LoggerFactory.getLogger(HuiyiController.class);

    @Autowired
    private HuiyiService huiyiService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("员工".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = huiyiService.queryPage(params);

        //字典表数据转换
        List<HuiyiView> list =(List<HuiyiView>)page.getList();
        for(HuiyiView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        HuiyiEntity huiyi = huiyiService.selectById(id);
        if(huiyi !=null){
            //entity转view
            HuiyiView view = new HuiyiView();
            BeanUtils.copyProperties( huiyi , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody HuiyiEntity huiyi, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,huiyi:{}",this.getClass().getName(),huiyi.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<HuiyiEntity> queryWrapper = new EntityWrapper<HuiyiEntity>()
            .eq("huiyi_name", huiyi.getHuiyiName())
            .eq("huiyi_address", huiyi.getHuiyiAddress())
            .eq("fangjianhao", huiyi.getFangjianhao())
            .eq("huiyineirong_text", huiyi.getHuiyineirongText())
            .eq("zhongyaorenyuan", huiyi.getZhongyaorenyuan())
            .eq("xiangqing_text", huiyi.getXiangqingText())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HuiyiEntity huiyiEntity = huiyiService.selectOne(queryWrapper);
        if(huiyiEntity==null){
            huiyi.setCreateTime(new Date());
            huiyiService.insert(huiyi);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody HuiyiEntity huiyi, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,huiyi:{}",this.getClass().getName(),huiyi.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<HuiyiEntity> queryWrapper = new EntityWrapper<HuiyiEntity>()
            .notIn("id",huiyi.getId())
            .andNew()
            .eq("huiyi_name", huiyi.getHuiyiName())
            .eq("huiyi_address", huiyi.getHuiyiAddress())
            .eq("fangjianhao", huiyi.getFangjianhao())
            .eq("huiyineirong_text", huiyi.getHuiyineirongText())
            .eq("zhongyaorenyuan", huiyi.getZhongyaorenyuan())
            .eq("xiangqing_text", huiyi.getXiangqingText())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HuiyiEntity huiyiEntity = huiyiService.selectOne(queryWrapper);
        if(huiyiEntity==null){
            huiyiService.updateById(huiyi);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        huiyiService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<HuiyiEntity> huiyiList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("../../upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            HuiyiEntity huiyiEntity = new HuiyiEntity();
//                            huiyiEntity.setHuiyiName(data.get(0));                    //会议名称 要改的
//                            huiyiEntity.setHuiyiAddress(data.get(0));                    //会议地点 要改的
//                            huiyiEntity.setFangjianhao(data.get(0));                    //房间号 要改的
//                            huiyiEntity.setHuiyineirongText(data.get(0));                    //会议内容 要改的
//                            huiyiEntity.setZhongyaorenyuan(data.get(0));                    //重要人员 要改的
//                            huiyiEntity.setXiangqingText(data.get(0));                    //详情 要改的
//                            huiyiEntity.setKaihuishijianTime(sdf.parse(data.get(0)));          //开会时间 要改的
//                            huiyiEntity.setCreateTime(date);//时间
                            huiyiList.add(huiyiEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        huiyiService.insertBatch(huiyiList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
