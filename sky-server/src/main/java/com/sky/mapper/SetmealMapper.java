package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
// ... existing code ...


@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    List<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    void deleteBatch(List<Long> ids);
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into setmeal (category_id,description,image,name,price,status,create_time, update_time, create_user, update_user) values (#{categoryId},#{description},#{image},#{name},#{price},#{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Setmeal setmeal);

    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
