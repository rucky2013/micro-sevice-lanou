package meng.xing.controller;

import meng.xing.TestItemType;
import meng.xing.controller.common.RequestIds;
import meng.xing.controller.common.ResponseStatus;
import meng.xing.entity.Subject;
import meng.xing.entity.TestItem;
import meng.xing.service.SubjectService;
import meng.xing.service.TestItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/testItems")
public class TestItemController {
    private final TestItemService testItemService;
    private final SubjectService subjectService;

    @Autowired
    public TestItemController(TestItemService testItemService, SubjectService subjectService) {
        this.testItemService = testItemService;
        this.subjectService = subjectService;
    }

    @GetMapping
    public Page<TestItem> getSomeTestItemsByType(
            @RequestParam(value = "type", defaultValue = "1") int type,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "order", defaultValue = "asc") String order,
            @RequestParam(value = "subject", required = false) String subject
    ) {
        Subject subjectObj = null;
        if (!Objects.equals(subject, "")) {
            subjectObj = subjectService.findSubjectByType(subject);
        }
        String typeStr;
        if (type == 1) {
            typeStr = TestItemType.QUESTION.toString();
        } else {
            typeStr = TestItemType.CHOICE.toString();
        }
        Sort _sort = new Sort(Sort.Direction.fromString(order), sort);
        //传来的页码是从1开始，而服务器从1开始算
        Pageable pageable = new PageRequest(page - 1, pageSize, _sort);
        return testItemService.findTestItemsByTypeAndSubject(typeStr, subjectObj, pageable);

    }

    @PatchMapping("/{id}")
    public ResponseStatus updateTestItem(@PathVariable("id") Long id, @RequestBody RequestTestItem requestTestItem) {
        ResponseStatus responseStatus = new ResponseStatus();
        TestItem testItem = testItemService.findTestItemById(id);
        testItem.setAnswer(requestTestItem.getAnswer());
        testItem.setQuestion(requestTestItem.getQuestion());
        testItem.setSubject(subjectService.findSubjectByType(requestTestItem.getSubject()));
        if (testItemService.updateTestItem(testItem)) {
            responseStatus.setMessage("修改试题成功");
            responseStatus.setSuccess("true");
            return responseStatus;
        }
        responseStatus.setMessage("修改试题失败");
        responseStatus.setSuccess("false");
        return responseStatus;
    }

    @PostMapping
    public ResponseStatus createTestItem(@RequestBody RequestTestItem requestTestItem) {
        ResponseStatus responseStatus = new ResponseStatus();
        TestItem testItem = new TestItem(
                requestTestItem.getType(),
                requestTestItem.getQuestion(),
                requestTestItem.getAnswer());
        testItem.setSubject(subjectService.findSubjectByType(requestTestItem.getSubject()));
        if (testItemService.addTestItme(testItem)) {
            responseStatus.setMessage("新增试题成功");
            responseStatus.setSuccess("true");
            return responseStatus;
        }
        responseStatus.setMessage("新增试题失败");
        responseStatus.setSuccess("false");
        return responseStatus;
    }

    @DeleteMapping("/{id}")
    public ResponseStatus deleteTestItemById(@PathVariable("id") Long id) {

        ResponseStatus responseStatus = new ResponseStatus();
        if (testItemService.deleteTestItemById(id)) {
            responseStatus.setMessage("删除试题成功!id:" + id);
            responseStatus.setSuccess("true");
            return responseStatus;
        }
        responseStatus.setMessage("删除试题失败!id:" + id);
        responseStatus.setSuccess("false");
        return responseStatus;
    }

    @DeleteMapping
    public ResponseStatus deleteTestItem(@RequestBody RequestIds ids) {

        List<Long> _ids = ids.getIds();
        ResponseStatus responseStatus = new ResponseStatus();
        try {
            _ids.forEach(testItemService::deleteTestItemById);
            responseStatus.setMessage("批量删除试题成功!ids:" + _ids);
            responseStatus.setSuccess("true");
            return responseStatus;
        } catch (Exception e) {
            responseStatus.setMessage("批量删除试题失败!ids:" + _ids);
            responseStatus.setSuccess("false");
            return responseStatus;
        }
    }

}

class RequestTestItem {
    private String type;
    private String question;
    private String answer;
    private String subject;

    public RequestTestItem() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}