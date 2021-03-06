package space.nyuki.questionnaire.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.nyuki.questionnaire.pojo.MemberGroup;
import space.nyuki.questionnaire.utils.JWTUtil;
import space.nyuki.questionnaire.utils.MapUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MemberGroupService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Transactional
	public void addData(MemberGroup memberGroup, String token) {
		String username = JWTUtil.getUsername(token);
		memberGroup.setCreatedAccount(username);
		memberGroup.setCreatedDate(new Date());
		mongoTemplate.save(memberGroup);
	}

	@Transactional
	public void updateData(MemberGroup memberGroup, String token) {
		String username = JWTUtil.getUsername(token);
		memberGroup.setEditedAccount(username);
		memberGroup.setEditedDate(new Date());
		Map<String, Object> stringObjectMap = MapUtil.objectToMap(memberGroup);
		Update update = new Update();
		stringObjectMap.forEach(update::set);
		mongoTemplate.findAndModify(
				Query.query(Criteria.where("_id").is(memberGroup.getId())),
				update,
				MemberGroup.class);

	}

	@Transactional
	public void updateEditDate(String token, String gid) {
		String username = JWTUtil.getUsername(token);
		Update update = new Update();
		assert username != null;
		update.set("edited_account", username);
		update.set("edited_time", new Date());
		mongoTemplate.findAndModify(Query.query(Criteria.where("_id").is(gid)), update, MemberGroup.class);
	}

	@Transactional
	public void deleteData(String id) {
		Update update = new Update();
		update.set("is_delete", 1);
		mongoTemplate.findAndModify(
				Query.query(Criteria.where("_id").is(id)),
				update,
				MemberGroup.class
		);
	}

	public List<MemberGroup> getData() {
		return mongoTemplate.find(Query.query(Criteria.where("is_delete").is(0)), MemberGroup.class);
	}

	public MemberGroup getDataById(String id) {
		return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id)), MemberGroup.class);
	}
	public List<String> getGroupName(List<String> idList){
		return idList.stream().map(this::getDataById)
				.map(MemberGroup::getGroupName)
				.collect(Collectors.toList());
	}
}
