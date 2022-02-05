package org.kryonite.kryoplayersync.paper.messaging.message;

import java.util.UUID;
import lombok.Data;

@Data
public class PlayerDataReady {

  private final UUID uniqueId;
  private final String sender;
}
