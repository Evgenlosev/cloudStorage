package netty;

import com.geekbrains.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private static Path currentPath;
    AuthService baseAuthService = new AuthService();

    public FileMessageHandler() {
        currentPath = Paths.get("server", "root");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListResponse(currentPath));
        ctx.writeAndFlush(new PathResponse(currentPath.toString()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command com) throws Exception {
        log.debug("received: {}", com.getType());
        switch (com.getType()) {
            case FILE_MESSAGE:
                FileMessage msg = (FileMessage) com;
                Files.write(currentPath.resolve(msg.getName()), msg.getBytes());
                ctx.writeAndFlush(new ListResponse(currentPath));
                log.debug("File received: {}", msg.getName());
                break;
            case FILE_REQUEST:
                FileRequest filereq = (FileRequest) com;
                FileMessage message = new FileMessage(currentPath.resolve(filereq.getName()));
                ctx.writeAndFlush(message);
                log.debug("File sent: {}", filereq.getName());
                break;
            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) com;
                Path newPath = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPath)) {
                    currentPath = newPath;
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                }
                break;
            case PATH_UP_REQUEST:
                if (currentPath.getParent() != null) {
                    currentPath = currentPath.getParent();
                }
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) com;
                String login = authRequest.getLogin();
                String pass = authRequest.getPass();
                AuthResponse authResponse = new AuthResponse();
                try {
                    if (baseAuthService.authentication(login, pass)) {
                        authResponse.setAuthOk(true);
                        currentPath = Paths.get("server","root");
                    } else {
                        authResponse.setAuthOk(false);
                    }
                } catch (Exception e){
                    log.error("Ошибка при обработке запроса на аутентификацию", e);
                }
                ctx.writeAndFlush(authResponse);
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
        }
    }
}
