package com.example.rl_v1.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.ui.theme.theme.Cream
import com.example.rl_v1.ui.theme.theme.LightGreen
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import kotlin.math.exp


/** Light green box with a bold [label] and, if not empty, a grey [value] under it. */
@Composable
fun PrimaryTextBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreen)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
            Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Spacer(Modifier.height(4.dp))

        if(value != "")
            Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }

}
/** Same as [PrimaryTextBox] but with a cream background. */
@Composable
fun SecondaryTextBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Cream)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Spacer(Modifier.height(4.dp))

        if(value != "")
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
    }

}

/** Large cream box with a bold [label] and its [value] under it. */
@Composable
fun SecondaryTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Cream, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Forest
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = GreyGreen
        )
    }
}

/** Large light green box with a bold [label] and its [value] under it. */
@Composable
fun PrimaryTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LightGreen, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Forest
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = GreyGreen
        )
    }
}

/** Small light green box with a small [label] above a larger [value]. */
@Composable
fun AltTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LightGreen, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = GreyGreen
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Forest
        )
    }
}

/** Text box the user can type in. Tapping anywhere in the box starts typing. */
@Composable
fun AppInputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreen)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { focusRequester.requestFocus() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Forest
        )

        Spacer(Modifier.height(4.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GreyGreen
                    )
                }
                innerTextField()
            }
        )
    }
}

/** Text box for a checklist of items; based on the above AppInputTextField */
@Composable
fun AppChecklistBox(
    label: String,
    items: List<String>,
    selectedItems: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LightGreen).padding(16.dp)){
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Forest
        )
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().clickable {
                val updated = selectedItems.toMutableSet()
                if (item in updated) {
                    updated.remove(item)
                } else {
                    updated.add(item)
                }
                onSelectionChange(updated)
            }, verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = item in selectedItems, onCheckedChange = { checked ->
                    val updated = selectedItems.toMutableSet()
                    if (checked) {
                        updated.add(item)
                    } else {
                        updated.remove(item)
                    }
                    onSelectionChange(updated)
                    }
                )
                Text(text = item, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


/** Dropdown Box stylized to be consistent with the UI theme; some code taken from https://developer.android.com/develop/ui/compose/components/menu*/
@Composable
fun DropdownBox(
    label: String,
    value: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(LightGreen)
        .padding(16.dp)
        .clickable(onClick = {expanded = !expanded})){

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Spacer(Modifier.height(4.dp))

        if(value != "")
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )


        Spacer(Modifier.height(8.dp))

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { campsiteOption ->
                DropdownMenuItem(
                    text = { Text(campsiteOption) },
                    onClick = {
                        onItemSelected(campsiteOption)
                    }
                )
            }
        }


    }


}
