package com.posada.santiago.gamapostsandcomments.application.bus;


import com.google.gson.Gson;
import com.posada.santiago.gamapostsandcomments.application.bus.models.CommentModel;
import com.posada.santiago.gamapostsandcomments.application.bus.models.Notification;
import com.posada.santiago.gamapostsandcomments.application.bus.models.PostModel;
import com.posada.santiago.gamapostsandcomments.application.controller.SocketController;
import com.posada.santiago.gamapostsandcomments.domain.events.CommentAdded;
import com.posada.santiago.gamapostsandcomments.domain.events.PostCreated;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RabbitMqConsumer {
    private final Gson gson = new Gson();
    private final SocketController controller;

    public static final String PROXY_QUEUE_POST_CREATED = "events.proxy.post.created";
    public static final String PROXY_QUEUE_COMMENT_ADDED = "events.proxy.comment.added";

    public RabbitMqConsumer(SocketController controller) {
        this.controller = controller;
    }

    @RabbitListener(queues = PROXY_QUEUE_POST_CREATED)
    public void listenToPostCratedQueue(String message) throws ClassNotFoundException {
        System.out.println(message);
        Notification notification = gson.fromJson(message, Notification.class);
        String type = notification.getType().replace("betapostsandcomments", "gamapostsandcomments");
        PostCreated postEvent = (PostCreated) gson.fromJson(notification.getBody(), Class.forName(type));
        PostModel post = new PostModel(postEvent.aggregateRootId(), postEvent.getAuthor(), postEvent.getTitle(), new ArrayList<>());
        controller.sendPostCreated("mainSpace", post);
    }

    @RabbitListener(queues = PROXY_QUEUE_COMMENT_ADDED)
    public void listenToCommentAddedQueue(String message) throws ClassNotFoundException {
        System.out.println(message);
        Notification notification = gson.fromJson(message, Notification.class);
        String type = notification.getType().replace("betapostsandcomments", "gamapostsandcomments");
        CommentAdded commentEvent = (CommentAdded) gson.fromJson(notification.getBody(), Class.forName(type));
        CommentModel comment = new CommentModel(commentEvent.getId(), commentEvent.aggregateRootId(), commentEvent.getAuthor(), commentEvent.getContent());
        controller.sendCommentAdded(commentEvent.aggregateRootId(), comment);
    }
}
