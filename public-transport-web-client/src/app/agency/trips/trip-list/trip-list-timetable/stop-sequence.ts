import {RouteStopGraph, RouteStopGraphNode} from "../../../../generated/public-transport-api";

export interface StopSequenceItem {
    stopId: number;
    name: string;
    variant: boolean;
    designation?: string;
}

export function buildStopSequence(graph?: RouteStopGraph | null): StopSequenceItem[] {
    if (!graph) {
        return [];
    }

    const nodesById = new Map<number, RouteStopGraphNode>();
    for (const node of graph.nodes ?? []) {
        if (node.stopId != null) {
            nodesById.set(node.stopId, node);
        }
    }

    const spineIds = graph.spineStopIds ?? [];
    if (spineIds.length === 0) {
        return (graph.nodes ?? [])
            .filter(node => node.stopId != null)
            .map(node => toItem(node.stopId!, node, false));
    }

    const insertionsAfter = new Map<number, StopSequenceItem[]>();
    const insertionsBefore = new Map<number, StopSequenceItem[]>();
    const insertionsAtStart: StopSequenceItem[] = [];

    for (const branch of graph.branches ?? []) {
        const variantStops = (branch.stopIds ?? [])
            .filter(stopId => stopId != null && !spineIds.includes(stopId))
            .map(stopId => toItem(stopId, nodesById.get(stopId), true, branch.variantDesignation));
        if (variantStops.length === 0) {
            continue;
        }
        if (branch.divergeFromStopId != null) {
            append(insertionsAfter, branch.divergeFromStopId, variantStops);
        } else if (branch.rejoinAtStopId != null) {
            append(insertionsBefore, branch.rejoinAtStopId, variantStops);
        } else {
            insertionsAtStart.push(...variantStops);
        }
    }

    const sequence: StopSequenceItem[] = [...insertionsAtStart];
    const seen = new Set(sequence.map(item => item.stopId));

    for (const stopId of spineIds) {
        addAll(sequence, seen, insertionsBefore.get(stopId));
        if (!seen.has(stopId)) {
            sequence.push(toItem(stopId, nodesById.get(stopId), false));
            seen.add(stopId);
        }
        addAll(sequence, seen, insertionsAfter.get(stopId));
    }

    for (const node of graph.nodes ?? []) {
        if (node.stopId != null && !seen.has(node.stopId)) {
            sequence.push(toItem(node.stopId, node, true));
            seen.add(node.stopId);
        }
    }

    return sequence;
}

function toItem(stopId: number, node: RouteStopGraphNode | undefined, variant: boolean, designation?: string): StopSequenceItem {
    return {
        stopId,
        name: node?.name ?? '',
        variant,
        designation: variant ? designation : undefined
    };
}

function append(target: Map<number, StopSequenceItem[]>, key: number, items: StopSequenceItem[]): void {
    const current = target.get(key) ?? [];
    current.push(...items);
    target.set(key, current);
}

function addAll(sequence: StopSequenceItem[], seen: Set<number>, items?: StopSequenceItem[]): void {
    if (!items) {
        return;
    }
    for (const item of items) {
        if (!seen.has(item.stopId)) {
            sequence.push(item);
            seen.add(item.stopId);
        }
    }
}
