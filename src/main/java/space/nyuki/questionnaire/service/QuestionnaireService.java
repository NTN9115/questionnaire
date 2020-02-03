package space.nyuki.questionnaire.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.nyuki.questionnaire.exception.CanNotEditException;
import space.nyuki.questionnaire.exception.ElementNotFoundException;
import space.nyuki.questionnaire.pojo.*;
import space.nyuki.questionnaire.utils.JWTUtil;
import space.nyuki.questionnaire.utils.MapUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author ning
 * @createTime 12/1/19 3:42 PM
 * @description 处理问卷调查表(表体)
 */
@Service
public class QuestionnaireService {
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private MemberService memberService;
	@Autowired
	private MemberGroupService memberGroupService;
	@Autowired
	private MailSenderService mailSenderService;
	@Autowired
	private MailSendScheduleService mailSendScheduleService;

	/**
	 * 创建问卷调查表
	 *
	 * @param questionnaire
	 */
	@Transactional
	public void createQuestionnaire(Questionnaire questionnaire, String token) {
		String uuid = questionnaire.getUuid();
		Questionnaire q = mongoTemplate.findOne(query((Criteria.where("uuid").is(uuid))),
				Questionnaire.class, "questionnaire");
		if (Objects.isNull(q)) {
			questionnaire.setCreatedTime(new Date());
			questionnaire.setCreatedAccount(JWTUtil.getUsername(token));
			questionnaire.setIsDelete(0);
			questionnaire.setIsEdit(0);
			mongoTemplate.save(questionnaire);
		}

	}


	/**
	 * 删除问卷调查
	 *
	 * @param id
	 */

	@Transactional
	public void deleteQuestionnaire(String id) {
		Update update = new Update();
		update.set("is_delete", 1);
		update.set("modify_time", new Date());
		mongoTemplate.updateFirst(query(Criteria.where("_id").is(id))
				, update, Questionnaire.class);
	}

	/**
	 * 修改问卷调查
	 *
	 * @param questionnaire
	 */
	@Transactional
	public void alterQuestionnaire(Questionnaire questionnaire, String token) {
		Questionnaire q = getQuestionnaireById(questionnaire.getId());
		Map<String, Object> map = MapUtil.objectToMap(questionnaire);
		Update update = new Update();
		map.forEach(update::set);
		update.set("edited_time", new Date());
		update.set("question_groups", questionnaire.getQuestionGroups());
		update.set("edited_account", Objects.requireNonNull(JWTUtil.getUsername(token)));

		mongoTemplate.updateFirst(
				query(Criteria.where("_id").is(questionnaire.getId())),
				update,
				Questionnaire.class);
	}

	/**
	 * 更新是否可编辑状态
	 *
	 * @param id
	 * @param isEdit
	 */
	@Transactional
	public void editChange(String id, Integer isEdit) {
		Update update = new Update();
		update.set("is_edit", isEdit);
		mongoTemplate.updateFirst(
				query(Criteria.where("_id").is(id)),
				update,
				Questionnaire.class
		);
	}

	/**
	 * 查询现有问卷调查
	 *
	 * @param page
	 * @param pageSize
	 * @return
	 */

	public PageContainer getQuestionnaire(Integer page, Integer pageSize) {
		Long total = getTotalNum();
		Query query = new Query();
		query.skip((page - 1) * pageSize);
		query.limit(pageSize);
		query.addCriteria(Criteria.where("is_delete").is(0));
		return new PageContainer(
				page,
				total,
				(int) (total / pageSize + 1),
				mongoTemplate.find(query, Questionnaire.class)
		);
	}

	public List<Questionnaire> getQuestionnaire() {
		Query query = new Query();
		query.addCriteria(Criteria.where("is_delete").is(0));
		return mongoTemplate.find(query, Questionnaire.class);
	}

	public List<Questionnaire> getQuestionnaire(Integer isEdit) {
		Query query = new Query();
		query.addCriteria(Criteria.where("is_delete").is(0));
		query.addCriteria(Criteria.where("is_edit").is(isEdit));
		return mongoTemplate.find(query, Questionnaire.class);
	}

	public Long getTotalNum() {
		Query query = new Query();
		query.addCriteria(Criteria.where("is_delete").is(0));
		return mongoTemplate.count(query, Questionnaire.class);
	}

	/**
	 * 分是否可编辑进行查询
	 *
	 * @param page
	 * @param pageSize
	 * @param isEdit
	 * @return
	 */
	public PageContainer getQuestionnaire(Integer page, Integer pageSize, Integer isEdit) {
		Long total = getTotalNum();
		Query query = new Query();
		query.skip((page - 1) * pageSize);
		query.limit(pageSize);
		query.addCriteria(Criteria.where("is_delete").is(0));
		query.addCriteria(Criteria.where("is_edit").is(isEdit));
		return new PageContainer(
				page,
				total,
				(int) (total / pageSize + 1),
				mongoTemplate.find(query, Questionnaire.class)
		);
	}

	/**
	 * 通过id查找问卷，并验证是否能够修改
	 *
	 * @param id
	 * @return
	 */

	public Questionnaire getQuestionnaireById(String id) {
		Questionnaire questionnaire = mongoTemplate.findOne(
				query(Criteria.where("_id").is(id)),
				Questionnaire.class
		);
		if (questionnaire.getIsEdit() == 1) {
			throw new CanNotEditException();
		}
		return questionnaire;
	}

	public Questionnaire getFinishQuestionnaireById(String id) {
		Questionnaire questionnaire = mongoTemplate.findOne(
				Query.query(Criteria.where("_id").is(id).and("is_edit").is(1)),
				Questionnaire.class
		);
		if (Objects.nonNull(questionnaire)) {
			return questionnaire;
		} else {
			throw new ElementNotFoundException(id);
		}
	}

	/**
	 * 设置更新时间
	 *
	 * @param update
	 */
	private void setUpdateTime(Update update) {
		update.set("edited_time", new Date());
	}

	/**
	 * 更新问卷内容
	 *
	 * @param id
	 * @param update
	 */
	public void setQuestionnaireUpdate(String id, Update update) {
		setUpdateTime(update);
		mongoTemplate.findAndModify(
				Query.query(Criteria.where("_id").is(id)),
				update,
				Questionnaire.class
		);
	}

	public void reverseDeleteQuestionnaire(String id) {
		Update update = new Update();
		update.set("is_delete", 0);
		update.set("modify_time", new Date());
		mongoTemplate.findAndModify(query(Criteria.where("_id").is(id))
				, update, Questionnaire.class);
	}

	@Transactional
	@Async("taskExecutor")
	public void createNewInstance(QuestionnaireCreate create, String token) {
		String username = JWTUtil.getUsername(token);
		String id = create.getQuestionnaireId();
		Questionnaire questionnaire = getFinishQuestionnaireById(id);
		QuestionnaireEntity questionnaireEntity = new QuestionnaireEntity();
		questionnaireEntity.setTitle(create.getName());
		questionnaireEntity.setCreatedAccount(username);
		questionnaireEntity.setFrom(create.getFrom());
		questionnaireEntity.setTo(create.getTo());
		questionnaireEntity.setIntroduce(questionnaire.getIntroduce());
		questionnaireEntity.setIsAnonymous(create.getIsAnonymous());
		questionnaireEntity.setPagination(create.getPagination());
		questionnaireEntity.setQuestionGroups(questionnaire.getQuestionGroups());
		if (create.getPagination() == 2) {
			questionnaireEntity.setPageSize(create.getPageSize());
		}
		if (create.getIsAnonymous() == 1) {

			List<String> memberId = create.getMemberId();
			List<Member> members = memberService.getMembersById(memberId);
			List<String> memberGroupNameList = memberGroupService.getGroupName(memberId);
			questionnaireEntity.setMembers(members);

			questionnaireEntity.setMemberGroupName(memberGroupNameList);
			Date sendMailTime = create.getSendMailTime();
			QuestionnaireEntity q = mongoTemplate.save(questionnaireEntity);
			if (sendMailTime.before(new Date())) {
				mailSenderService.sendMail(members, q);
			} else {
				mailSendScheduleService.addSchedule(members, memberGroupNameList, q, sendMailTime);
			}
		} else {
			mongoTemplate.save(questionnaireEntity);
		}


	}
}