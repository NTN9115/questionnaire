package space.nyuki.questionnaire.pojo.answer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;

import java.util.List;

/**
 * @author ning
 * @createTime 12/1/19 1:46 PM
 * @description 不同类型答案的父类
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "type"
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = Choice.class, name = "choice"),
		@JsonSubTypes.Type(value = Comment.class, name = "comment"),
		@JsonSubTypes.Type(value = InquiryDate.class, name = "date")
})
@ApiModel("答题方框，不包含问题")
@JsonView(Object.class)
public interface AnswerCell {
	void setIndex(List<Integer> index);
}