package com.x8bit.bitwarden.ui.tools.feature.generator.passgen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bitwarden.ui.platform.components.button.BitwardenStandardIconButton
import com.bitwarden.ui.platform.components.card.BitwardenInfoCalloutCard
import com.bitwarden.ui.platform.components.field.BitwardenPasswordField
import com.bitwarden.ui.platform.components.field.BitwardenTextField
import com.bitwarden.ui.platform.components.model.CardStyle
import com.bitwarden.ui.platform.components.segment.BitwardenSegmentedButton
import com.bitwarden.ui.platform.components.segment.SegmentedButtonState
import com.bitwarden.ui.platform.components.slider.BitwardenSlider
import com.bitwarden.ui.platform.components.toggle.BitwardenSwitch
import com.bitwarden.ui.platform.resource.BitwardenDrawable
import com.bitwarden.ui.platform.resource.BitwardenString
import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.ui.tools.feature.generator.PassgenAction
import com.x8bit.bitwarden.ui.tools.feature.generator.PassgenMainType
import kotlinx.collections.immutable.persistentListOf

/**
 * Options for the fork-only passgen generator type. See FORK.md.
 */
@Suppress("LongMethod")
@Composable
fun PassgenContent(
    state: PassgenMainType,
    onAction: (PassgenAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        state.errorMessage?.let {
            BitwardenInfoCalloutCard(
                text = it,
                modifier = Modifier
                    .testTag("PassgenError")
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        BitwardenSegmentedButton(
            options = persistentListOf(
                SegmentedButtonState(
                    text = stringResource(R.string.passgen_version_2),
                    onClick = { onAction(PassgenAction.VersionChange(version = 2)) },
                    isChecked = state.version == 2,
                    testTag = "PassgenVersionV2",
                ),
                SegmentedButtonState(
                    text = stringResource(R.string.passgen_version_3),
                    onClick = { onAction(PassgenAction.VersionChange(version = 3)) },
                    isChecked = state.version == 3,
                    testTag = "PassgenVersionV3",
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))

        BitwardenPasswordField(
            label = stringResource(R.string.passgen_passphrase),
            value = state.passphrase,
            onValueChange = { onAction(PassgenAction.PassphraseChange(it)) },
            supportingContent = null,
            cardStyle = CardStyle.Top(),
            passwordFieldTestTag = "PassgenPassphraseField",
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.passphrase.isEmpty() && state.passphraseUsed.isNotEmpty()) {
            BitwardenTextField(
                label = stringResource(R.string.passgen_passphrase_used),
                value = state.passphraseUsed,
                onValueChange = {},
                readOnly = true,
                supportingText = stringResource(R.string.passgen_passphrase_used_hint),
                cardStyle = CardStyle.Middle(),
                textFieldTestTag = "PassgenPassphraseUsedField",
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    BitwardenStandardIconButton(
                        vectorIconRes = BitwardenDrawable.ic_copy,
                        contentDescription = stringResource(R.string.passgen_copy_passphrase),
                        onClick = { onAction(PassgenAction.CopyPassphraseUsedClick) },
                    )
                },
            )
        }
        BitwardenTextField(
            label = stringResource(R.string.passgen_salt),
            value = state.salt,
            onValueChange = { onAction(PassgenAction.SaltChange(it)) },
            supportingText = stringResource(R.string.passgen_salt_hint),
            cardStyle = CardStyle.Bottom,
            textFieldTestTag = "PassgenSaltField",
            modifier = Modifier.fillMaxWidth(),
            actions = {
                BitwardenStandardIconButton(
                    vectorIconRes = BitwardenDrawable.ic_copy,
                    contentDescription = stringResource(R.string.passgen_copy_salt),
                    onClick = { onAction(PassgenAction.CopySaltClick) },
                )
            },
        )
        Spacer(modifier = Modifier.height(8.dp))

        BitwardenSlider(
            value = state.length,
            onValueChange = { length, isUserInteracting ->
                onAction(PassgenAction.LengthChange(length, isUserInteracting))
            },
            range = PassgenMainType.LENGTH_MIN..PassgenMainType.LENGTH_MAX,
            sliderTag = "PassgenLengthSlider",
            valueTag = "PassgenLengthLabel",
            cardStyle = CardStyle.Full,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))

        BitwardenSwitch(
            label = "A—Z",
            contentDescription = stringResource(BitwardenString.uppercase_ato_z),
            isChecked = state.useUppers,
            onCheckedChange = { onAction(PassgenAction.ToggleUppers(it)) },
            cardStyle = CardStyle.Top(),
            modifier = Modifier.fillMaxWidth().testTag("PassgenUppersToggle"),
        )
        BitwardenSwitch(
            label = "0-9",
            contentDescription = stringResource(BitwardenString.numbers_zero_to_nine),
            isChecked = state.useNumbers,
            onCheckedChange = { onAction(PassgenAction.ToggleNumbers(it)) },
            cardStyle = CardStyle.Middle(),
            modifier = Modifier.fillMaxWidth().testTag("PassgenNumbersToggle"),
        )
        BitwardenSwitch(
            label = stringResource(BitwardenString.special_characters),
            isChecked = state.useSpecials,
            onCheckedChange = { onAction(PassgenAction.ToggleSpecials(it)) },
            cardStyle = CardStyle.Middle(),
            modifier = Modifier.fillMaxWidth().testTag("PassgenSpecialsToggle"),
        )
        if (state.useSpecials) {
            BitwardenTextField(
                label = stringResource(R.string.passgen_custom_specials),
                value = state.customSpecials,
                onValueChange = { onAction(PassgenAction.CustomSpecialsChange(it)) },
                supportingText = stringResource(R.string.passgen_custom_specials_hint),
                cardStyle = CardStyle.Middle(),
                textFieldTestTag = "PassgenCustomSpecialsField",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        BitwardenSwitch(
            label = stringResource(R.string.passgen_avoid_ambiguous),
            isChecked = state.avoidAmbiguous,
            onCheckedChange = { onAction(PassgenAction.ToggleAvoidAmbiguous(it)) },
            cardStyle = if (state.avoidAmbiguous) CardStyle.Middle() else CardStyle.Bottom,
            modifier = Modifier.fillMaxWidth().testTag("PassgenAvoidAmbiguousToggle"),
        )
        if (state.avoidAmbiguous) {
            BitwardenTextField(
                label = stringResource(R.string.passgen_custom_ambiguous),
                value = state.customAmbiguous,
                onValueChange = { onAction(PassgenAction.CustomAmbiguousChange(it)) },
                supportingText = stringResource(R.string.passgen_custom_ambiguous_hint),
                cardStyle = CardStyle.Bottom,
                textFieldTestTag = "PassgenCustomAmbiguousField",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
