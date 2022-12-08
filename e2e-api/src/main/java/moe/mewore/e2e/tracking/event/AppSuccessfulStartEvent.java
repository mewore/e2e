package moe.mewore.e2e.tracking.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AppSuccessfulStartEvent implements AppStartEvent {

    private final String protocol;

    private final int port;
}
