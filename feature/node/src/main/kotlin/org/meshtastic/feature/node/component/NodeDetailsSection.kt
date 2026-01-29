/*
 * Copyright (c) 2025-2026 Meshtastic LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.meshtastic.feature.node.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.meshtastic.core.database.entity.Packet
import org.meshtastic.core.database.model.Node
import org.meshtastic.core.model.util.formatUptime
import org.meshtastic.core.strings.Res
import org.meshtastic.core.strings.details
import org.meshtastic.core.strings.encryption_error
import org.meshtastic.core.strings.encryption_error_text
import org.meshtastic.core.strings.hops_out_of_total
import org.meshtastic.core.strings.node_number
import org.meshtastic.core.strings.node_sort_last_heard
import org.meshtastic.core.strings.relayed_by
import org.meshtastic.core.strings.role
import org.meshtastic.core.strings.short_name
import org.meshtastic.core.strings.uptime
import org.meshtastic.core.strings.user_id
import org.meshtastic.core.ui.util.formatAgo

@Composable
fun NodeDetailsSection(node: Node, modifier: Modifier = Modifier, nodeMap: Map<Int, Node> = emptyMap(), ourNode: Node? = null, showRelayInfo: Boolean = false) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        SelectionContainer {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(Res.string.details),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(20.dp))

                if (node.mismatchKey) {
                    MismatchKeyWarning()
                    Spacer(Modifier.height(20.dp))
                }

                MainNodeDetails(node = node, nodeMap = nodeMap, ourNode = ourNode, showRelayInfo = showRelayInfo)
            }
        }
    }
}

@Composable
private fun MismatchKeyWarning() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.KeyOff,
                    contentDescription = stringResource(Res.string.encryption_error),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.encryption_error),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.encryption_error_text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MainNodeDetails(node: Node, nodeMap: Map<Int, Node>, ourNode: Node?, showRelayInfo: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItem(
                label = stringResource(Res.string.short_name),
                value = node.user.shortName.ifEmpty { "???" },
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
            )
            InfoItem(
                label = stringResource(Res.string.role),
                value = node.user.role.name,
                icon = Icons.Default.Work,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItem(
                label = stringResource(Res.string.node_sort_last_heard),
                value = formatAgo(node.lastHeard),
                icon = Icons.Default.History,
                modifier = Modifier.weight(1f),
            )
            InfoItem(
                label = stringResource(Res.string.node_number),
                value = node.num.toUInt().toString(),
                icon = Icons.Default.Numbers,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItem(
                label = stringResource(Res.string.user_id),
                value = node.user.id,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
            )
            if (node.deviceMetrics.uptimeSeconds > 0) {
                InfoItem(
                    label = stringResource(Res.string.uptime),
                    value = formatUptime(node.deviceMetrics.uptimeSeconds),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        // Show relay information if available (only when setting is enabled and node was relayed)
        // Direct nodes (hopsAway == 0) already show SNR/RSSI which implies direct connection
        if (showRelayInfo && node.hopsAway > 0) node.relayNode?.let { relayNodeId ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            val nodes = nodeMap.values.toList()
            val relayNodeIdSuffix = relayNodeId and Packet.RELAY_NODE_SUFFIX_MASK
            val hexByte = "0x%02X".format(relayNodeIdSuffix)

            // Find all candidate nodes
            val candidateRelayNodes = nodes.filter {
                it.num != ourNode?.num &&
                    it.lastHeard != 0 &&
                    (it.num and Packet.RELAY_NODE_SUFFIX_MASK) == relayNodeIdSuffix
            }

            // Get the best relay node candidate using signal quality (SNR/RSSI)
            // Higher SNR is better, less negative RSSI is better
            val bestRelayNode = if (candidateRelayNodes.size == 1) {
                candidateRelayNodes.first()
            } else {
                candidateRelayNodes.maxByOrNull { candidate ->
                    // Combine SNR and RSSI for scoring - SNR weighted more heavily
                    // SNR typically ranges from -20 to +10, RSSI from -120 to -30
                    (candidate.snr * 3) + (candidate.rssi + 120)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                bestRelayNode?.let { relayNode ->
                    val relayText = if (candidateRelayNodes.size > 1) {
                        // Sort candidates by signal quality (best first)
                        val candidateNames = candidateRelayNodes
                            .sortedByDescending { (it.snr * 3) + (it.rssi + 120) }
                            .joinToString(", ") { it.user.shortName }
                        "$candidateNames ($hexByte)"
                    } else {
                        "${relayNode.user.longName} ($hexByte)"
                    }
                    InfoItem(
                        label = "Last Relay",
                        value = relayText,
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                    )
                } ?: run {
                    InfoItem(
                        label = "Last Relay",
                        value = hexByte,
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (node.hopsAway > 0) {
                    val hopText = if (node.hopStart > 0) {
                        stringResource(Res.string.hops_out_of_total, node.hopsAway, node.hopStart)
                    } else {
                        "${node.hopsAway} hops"
                    }
                    InfoItem(
                        label = "Hops",
                        value = hopText,
                        icon = Icons.Default.Numbers,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
