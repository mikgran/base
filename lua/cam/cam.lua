
local CAM, Cam = ...
_G[CAM] = Cam

LibStub('AceAddon-3.0'):NewAddon(Cam, CAM, 'AceHook-3.0', 'AceEvent-3.0', 'AceConsole-3.0')
local AceGUI = LibStub("AceGUI-3.0")

Cam.camFrame = nil
Cam.buttons = {}
Cam.toggleDelete = false -- always false at the start

function Cam:OnInitialize()

    -- persisted with: ## SavedVariables: CamAceDB
    self.db = LibStub("AceDB-3.0"):New("CamAceDB")

    local playerName = UnitName("Player")
    Cam:AddNameToDB(playerName)
end

function Cam:OnEnable()
    self:SecureHook("ContainerFrameItemButton_OnModifiedClick")
end

function Cam:OnDisable()
-- Called when the Cam is disabled
end

function Cam:ContainerFrameItemButton_OnModifiedClick(...)

    if select(2, ...) == "LeftButton" and IsShiftKeyDown() and not CursorHasItem() then

        if MailFrame:IsVisible() then

            MailFrameTab2:Click("LeftButton", down)

            -- if recipient is not yet in the list, add it to the frame
            local recipient = SendMailNameEditBox:GetText()

            if Cam:NameExistsInDB(recipient) == false and recipient ~= "" then

                Cam:AddNameToDB(recipient)
                Cam:NilCamFrameResources()
            end

            if Cam.camFrame == nil then
                Cam.camFrame = Cam:GetCamFrame()
                Cam.toggleDelete = false
            end

            -- send the mail
            -- PickupContainerItem(self.bag, self.slot)
            -- ClickSendMailItemButton()

            -- cycle buttons forward
            Cam:HandleNextButtonUpdate()

        end

    end

end

function Cam:NameExistsInDB(name)

    if type(self.db.global.chars) ~= "table" or name == nil or self.db.global.chars[name] == nil then

        return false
    end
    return true
end

function Cam:AddNameToDB(name)

    Cam:DebugPrint({"name", name})

    if type(self.db.global.chars) ~= "table" then

        self.db.global.chars = {}
    end

    -- unique names with 2 or more characters accepted
    if name ~= nil and type(name) == "string" and #name > 1 then

        self.db.global.chars[name] = 1
    end

end

function Cam:GetCamFrame()

    local frameWidth = 255
    local frame = AceGUI:Create("Frame")
    frame:SetTitle("Cam")
    frame:SetStatusText("")
    frame:SetCallback("OnClose", function(widget) AceGUI:Release(widget); Cam:NilCamFrameResources() end)
    frame:SetLayout("Flow")
    frame:SetWidth(frameWidth)
    frame:SetHeight(self:GetCamHeight())
    frame:EnableResize(false)
    frame:SetPoint("TOPRIGHT", MailFrame, frameWidth + 20, 0)

    self:RegisterEvent("MAIL_CLOSED", "OnMailClose", frame)

    local sanityCheck = 20 -- maximum number of alts for 2 accounts.

    for key, value in pairsByKeys(self.db.global.chars) do

        local button = Cam:CreateButton(key)

        Cam:AddButtonToListIfNotExists(key, button, value)

        -- create a check box for determining if the name will be used in the cycle
        local checkBox = Cam:CreateLinkedCheckBox(key, value)

        frame:AddChild(button)
        frame:AddChild(checkBox)

        sanityCheck = sanityCheck - 1
        if sanityCheck <= 0 then
            break
        end

    end

    local deleteToggleCheckBox = Cam:CreateCheckBox("Delete list names")
    frame:AddChild(deleteToggleCheckBox)

    return frame
end

function Cam:ButtonOnClick(button, key)

    -- toggleDelete == false: clicking buttons puts the name into recipient box and button is starred
    -- toggleDelete == true: clicking buttons removes a name from db and button cache
    if Cam.toggleDelete then

        Cam:Print("Deleting name: "..key)
        if Cam:NameExistsInDB(key) then

            Cam.buttons[key] = nil
            self.db.global.chars[key] = nil

            Cam:NilCamFrameResources()
            Cam.camFrame = Cam:GetCamFrame()
        end

    else

        SendMailNameEditBox:SetText(key)
        self.db.global.currentSelectedKey = key
        Cam:ResetTextsInButtons()
        Cam:SetButtonText(button, key.." *")
    end
end

function Cam:ToggleDeleteCheckBoxOnValueChanged(component)

    if Cam.toggleDelete then
        Cam.toggleDelete = false
        Cam:Print("Deleting off.")
    else
        Cam.toggleDelete = true
        Cam:Print("Deleting on: Clicking buttons deletes them from the list.")
    end

end

function Cam:LinkedCheckBoxOnValueChanged(component, key)

    Cam:Print("CheckBox clicked for: "..key)

    -- TODO
    -- update buttons states
    -- traverse forward in button list

end

function Cam:NilCamFrameResources()
    if Cam.camFrame then
        Cam.camFrame:Hide()
    end
    Cam.camFrame = nil
    Cam.buttons = nil
    Cam.buttons = {}
    Cam.toggleDelete = false
end

function Cam:OnMailClose(frame)
    Cam:NilCamFrameResources()
    frame:Hide()
end

-- toimprove: replace all for iterations with a generic iteration and a lambda call
function Cam:GetCamHeight()

    -- TODO fix meee, heights!
    local numberOfKeys = 0
    local rowHeight = 35
    local bottomSpaceHeight = 90

    for key, value in pairsByKeys(self.db.global.chars) do

        numberOfKeys = numberOfKeys + 1
    end

    return numberOfKeys * rowHeight + bottomSpaceHeight
end

function Cam:AddButtonToListIfNotExists(key, button, active)

    Cam:DebugPrint({ "key", key }, { "button", button }, { "active", active })

    if Cam.buttons[key] == nil then

        Cam.buttons[key] = { button, active }
    end
end

function Cam:CreateCheckBox(labelText)

    local checkBox = AceGUI:Create("CheckBox")
    checkBox:SetLabel(labelText)
    checkBox:SetValue(false)
    checkBox:SetWidth(120)
    checkBox:SetCallback("OnValueChanged", function(button) Cam:ToggleDeleteCheckBoxOnValueChanged(checkBox) end)

    return checkBox
end

function Cam:CreateLinkedCheckBox(key, value)

    local checkBox = AceGUI:Create("CheckBox")
    checkBox:SetValue(value == 1)
    checkBox:SetWidth(20)
    checkBox:SetCallback("OnValueChanged", function(button) Cam:LinkedCheckBoxOnValueChanged(checkBox, key) end)

    return checkBox
end

function Cam:CreateButton(key)

    local button = AceGUI:Create("Button")
    button:SetCallback("OnClick", function(button) Cam:ButtonOnClick(button, key) end)
    Cam:SetButtonText(button, key)

    return button
end

-- a, b, c, d, rules: if selected a->b, b->c, c->d, d->a, etc
function Cam:HandleNextButtonUpdate()

    if Cam:tableLength(Cam.buttons) == 0 or Cam.buttons == nil then
        return
    end

    -- collect first key
    local firstKey = nil
    for key, value in pairsByKeys(Cam.buttons) do firstKey = key; break end

    -- set current key to first key in case selected key now refers to nothing or invalid key
    if self.db.global.currentSelectedKey == nil or Cam.buttons[self.db.global.currentSelectedKey] == nil then
        self.db.global.currentSelectedKey = firstKey
    end

    Cam:ResetTextsInButtons()

    local foundKey = false
    local useFirst = true
    for key, value in pairsByKeys(Cam.buttons) do

        if foundKey then

            Cam:SetHighlightForKeyAndSetRecipient(key)

            self.db.global.currentSelectedKey = key
            useFirst = false;
            break
        end

        if key == self.db.global.currentSelectedKey then

            self.db.global.currentSelectedKey = nil
            foundKey = true -- next loop key is the next highlighted, if looping out of array, use the first
        end
    end

    if useFirst then

        Cam:SetHighlightForKeyAndSetRecipient(firstKey)
    end
end

function Cam:SetHighlightForKeyAndSetRecipient(key)

    local button = Cam.buttons[key][1]
    Cam:SetButtonText(button, key.." *")
    SendMailNameEditBox:SetText(key)
end

function Cam:SetButtonText(button, text)

    Cam:DebugPrint({ "button", button }, { "text", text })

    local font = button.frame:CreateFontString()
    font:SetFont("Fonts/FRIZQT__.TTF", 14)
    font:SetTextColor(1, 1, 1, 1.0);
    font:SetText(text)
    button.frame:SetFontString(font)
    button.text = font
end

function Cam:ResetTextsInButtons()

    for key, value in pairsByKeys(Cam.buttons) do

        local button = Cam.buttons[key][1]
        Cam:SetButtonText(button, key)
    end
end


function Cam:tableLength(t)
    local count = 0
    for _ in pairs(t) do count = count + 1 end
    return count
end

function pairsByKeys (t, f)
    local a = {}
    for n in pairs(t) do table.insert(a, n) end
    table.sort(a, f)
    local i = 0      -- iterator variable
    local iter = function ()   -- iterator function
        i = i + 1
        if a[i] == nil then return nil
        else return a[i], t[a[i]]
        end
    end
    return iter
end

function Cam:DebugPrint(...)

    local printOnce = true
    for k,v in pairs({...}) do

        -- { { "nameOfVariable", variable }, "nameOfVariable", variable } }
        if v[2] == nil then

            if printOnce then

                Cam:Print(debugstack(1, 4, 4))
                Cam:Print("Illegal arguments got: ")
                printOnce = false
            end

            Cam:Print(""..tostring(v[1])..": nil")
        end
    end
end

function Cam:PrintWithPrefix(prefixCharacter, key, value)

    Cam:Print((prefixCharacter or "-").." key: "..(key or "").." value: "..type(value))
end

function Cam:PrintTable(table, prefixCharacter)

    for key, value in pairsByKeys(table) do

        Cam:PrintWithPrefix(prefixCharacter, key, value)

    end

end

function Cam:PrintTableRecursive(component, prefixCharacter, currentDepth, maxDepth)

    if currentDepth == nil or maxDepth == nil or currentDepth >= maxDepth then
        return
    end

    for key, value in pairsByKeys(component) do

        Cam:PrintWithPrefix(prefixCharacter, key, value)

        if type(value) == "table" then

            Cam:PrintTableRecursive(value, prefixCharacter..prefixCharacter, (currentDepth or 0) + 1, (maxDepth or 1))
        end
    end

end



