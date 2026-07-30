package com.visibleai.brasstacks.task.dto;

import com.visibleai.brasstacks.model.Task;

public record DoneRequest(Task.EnergyLevel energyLevel) {}
