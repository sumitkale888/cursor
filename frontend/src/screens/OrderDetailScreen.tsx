import React, {useCallback, useEffect, useState} from 'react';
import {
  FlatList,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as orderApi from '../api/orderApi';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {OrdersStackParamList} from '../navigation/types';
import {Order, OrderItem} from '../types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';
import {formatCurrency, formatDate} from '../utils/format';

type Props = NativeStackScreenProps<OrdersStackParamList, 'OrderDetail'>;

export function OrderDetailScreen({navigation, route}: Props) {
  const {orderId} = route.params;
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadOrder = useCallback(async () => {
    setError(null);
    try {
      const data = await orderApi.getOrderDetails(orderId);
      setOrder(data);
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error || !order) {
    return (
      <ErrorView message={error ?? 'Order not found'} onRetry={loadOrder} />
    );
  }

  const renderItem = ({item}: {item: OrderItem}) => (
    <View style={styles.item}>
      <View style={styles.itemInfo}>
        <Text style={styles.itemName}>{item.productName}</Text>
        <Text style={styles.itemQty}>
          {formatCurrency(item.price)} x {item.quantity}
        </Text>
      </View>
      <Text style={styles.itemSubtotal}>
        {formatCurrency(item.subtotal)}
      </Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <Header
        title={`Order #${order.id}`}
        subtitle={formatDate(order.createdAt)}
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.statusCard}>
          <Text style={styles.statusLabel}>Status</Text>
          <Text style={styles.statusValue}>{order.status}</Text>
        </View>

        <Text style={styles.sectionTitle}>Items</Text>
        <FlatList
          data={order.items}
          keyExtractor={item => String(item.id)}
          renderItem={renderItem}
          scrollEnabled={false}
        />

        <View style={styles.totalCard}>
          <Text style={styles.totalLabel}>Total Paid</Text>
          <Text style={styles.totalValue}>
            {formatCurrency(order.totalAmount)}
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.lg,
    paddingBottom: spacing.xxxl,
  },
  statusCard: {
    backgroundColor: colors.primaryLight,
    borderRadius: radius.lg,
    padding: spacing.lg,
    marginBottom: spacing.lg,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  statusLabel: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  statusValue: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.primary,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.md,
  },
  item: {
    flexDirection: 'row',
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    padding: spacing.md,
    marginBottom: spacing.sm,
    borderWidth: 1,
    borderColor: colors.border,
  },
  itemInfo: {
    flex: 1,
  },
  itemName: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  itemQty: {
    fontSize: 13,
    color: colors.textSecondary,
  },
  itemSubtotal: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
  },
  totalCard: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.lg,
    marginTop: spacing.lg,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: colors.border,
  },
  totalLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
  },
  totalValue: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.primary,
  },
});
