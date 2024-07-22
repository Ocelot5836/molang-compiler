import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import org.junit.jupiter.api.Test;

public class TestRealExamples {
    private static String tests = """
q.get_equipped_item_name=='milk_bucket'?0:2
!v.is_holding_right?math.cos(q.life_time*180)*1.6
q.get_equipped_item_name(0)==''?math.cos(q.anim_time)*2.6
math.sin(q.rotation_to_camera(1)-q.head_y_rotation(0))<-0.4?1.1
math.round(math.clamp(q.rotation_to_camera(0)*-0.03,-1,!q.is_angry))
math.sin(q.rotation_to_camera(1)-q.head_y_rotation(0))>0.4?-1.1
math.round(math.clamp(q.rotation_to_camera(0)*-0.03,-1,!q.is_angry))
32+query.get_root_locator_offset('armor_offset.helmet',1)
c.item_slot=='main_hand'?0.6:-0.6
c.item_slot=='main_hand'?0.5:-0.5
c.item_slot=='main_hand'?90:-90
c.item_slot=='main_hand'?-1.5:1.5
c.item_slot=='main_hand'?85:-85
c.item_slot=='main_hand'?4:-4
c.item_slot=='main_hand'?42:-42
c.item_slot=='main_hand'?-110:110
c.item_slot=='main_hand'?1:-1
c.item_slot=='main_hand'?10:-10
c.item_slot=='main_hand'?-45:45
c.item_slot=='main_hand'?190:-190
c.item_slot=='main_hand'?0.6:-0.6
-this+query.get_default_bone_pivot('body',1)-query.get_default_bone_pivot('waist',1)
-query.get_root_locator_offset('armor_offset.default_neck','y')
!q.is_item_name_any('slot.weapon.mainhand','minecraft:torch','minecraft:soul_torch','minecraft:lantern','minecraft:soul_lantern')?-90-math.sin(query.anim_time*79.2)*32:0
!q.is_item_name_any('slot.weapon.mainhand','minecraft:torch','minecraft:soul_torch','minecraft:lantern','minecraft:soul_lantern')?50+math.cos(query.anim_time*79.2)*20:0
q.is_item_name_any('slot.weapon.mainhand','minecraft:trident')?0:45+math.cos(query.anim_time*190)*20
q.is_item_name_any('slot.weapon.mainhand','minecraft:trident')?0:-45+math.cos(query.anim_time*190)*-20
math.mod(q.life_time,1.7)>1.55?-2
query.get_root_locator_offset('armor_offset.default_neck', 1)
q.is_item_name_any('slot.weapon.mainhand','minecraft:torch','minecraft:soul_torch','minecraft:lantern','minecraft:soul_lantern')?0:-this*(q.anim_time*-q.anim_time+1)
query.get_root_locator_offset('armor_offset.helmet',1)
!query.is_moving
!query.is_moving
q.any_animation_finished||!q.is_on_ground
!q.is_on_ground||q.modified_move_speed<0.86
!q.is_on_ground
!q.is_on_ground&&!q.is_in_water&&q.vertical_speed!=0.0
!query.is_on_ground&&!query.is_in_water&&query.vertical_speed>0
!q.is_on_ground
!q.is_on_ground
q.is_on_ground&&!q.is_in_water
!q.is_on_fire
!query.is_moving
!query.is_moving
q.any_animation_finished||!q.is_on_ground
!q.is_on_ground||q.modified_move_speed<0.86
!q.is_on_ground
!q.is_on_ground&&!q.is_in_water&&q.vertical_speed!=0.0
!query.is_on_ground&&!query.is_in_water&&query.vertical_speed>0
!q.is_on_ground
!q.is_on_ground
q.is_on_ground&&!q.is_in_water
!q.is_on_fire
!query.is_moving
!query.is_moving
q.any_animation_finished||!q.is_on_ground
!q.is_on_ground||q.modified_move_speed<0.86
!q.is_on_ground
!q.is_on_ground&&!q.is_in_water&&q.vertical_speed!=0.0
!query.is_on_ground&&!query.is_in_water&&query.vertical_speed>0
!q.is_on_ground
!q.is_on_ground
q.is_on_ground&&!q.is_in_water
!q.is_on_fire
!query.is_moving
!query.is_moving
q.any_animation_finished||!q.is_on_ground
!q.is_on_ground||q.modified_move_speed<0.86
!q.is_on_ground
!q.is_on_ground&&!q.is_in_water&&q.vertical_speed!=0.0
!query.is_on_ground&&!query.is_in_water&&query.vertical_speed>0
!q.is_on_ground
!q.is_on_ground
q.is_on_ground&&!q.is_in_water
!q.is_on_fire
!query.is_moving
!query.is_moving
q.any_animation_finished||!q.is_on_ground
!q.is_on_ground||q.modified_move_speed<0.86
!q.is_on_ground
!q.is_on_ground&&!q.is_in_water&&q.vertical_speed!=0.0
!query.is_on_ground&&!query.is_in_water&&query.vertical_speed>0
!q.is_on_ground
!q.is_on_ground
q.is_on_ground&&!q.is_in_water
!q.is_on_fire
""";

    @Test
    public void testRealExamples() throws MolangSyntaxException {
        for (String testString : tests.split("\n")) {
            if(testString.isBlank()) continue;
            test(testString);
        }
    }

    private void test(String query) throws MolangSyntaxException {
        MolangCompiler compiler = MolangCompiler.create();

        compiler.compile(query);
    }

}
