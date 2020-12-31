package CA.ROBBYNATOS;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.plugin.Plugin;

public class CarpetToggleMessagePrompt extends MessagePrompt {

    private Plugin plugin;
    boolean isCarpetOn;

    public CarpetToggleMessagePrompt(Plugin plugin, boolean isCarpetOn){
        this.plugin = plugin;
        this.isCarpetOn = isCarpetOn;
    }

    @Override
    protected Prompt getNextPrompt(ConversationContext conversationContext) {
        return END_OF_CONVERSATION;
    }

    @Override
    public String getPromptText(ConversationContext conversationContext) {
        return ChatColor.YELLOW + "Magic Carpet: " + (isCarpetOn ? ChatColor.GREEN + "ON": ChatColor.RED + "OFF");
    }
}
